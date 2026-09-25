package br.com.conciliacao.service;

import br.com.conciliacao.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PedidoService {
  private final PedidoRepository repository;
  private final EmpresaRepository empresaRepository;
  private final ProdutoRepository produtoRepository;
  private final TituloRepository tituloRepository;
  private final FormaPagamentoRepository formaPagamentoRepository;
  private final TransportadoraRepository transportadoraRepository;
  private final DestinoPagamentoService destinoPagamentoService;

  public PedidoService(PedidoRepository repository, EmpresaRepository empresaRepository,
      ProdutoRepository produtoRepository, TituloRepository tituloRepository,
      FormaPagamentoRepository formaPagamentoRepository, TransportadoraRepository transportadoraRepository,
      DestinoPagamentoService destinoPagamentoService) {
    this.repository = repository;
    this.empresaRepository = empresaRepository;
    this.produtoRepository = produtoRepository;
    this.tituloRepository = tituloRepository;
    this.formaPagamentoRepository = formaPagamentoRepository;
    this.transportadoraRepository = transportadoraRepository;
    this.destinoPagamentoService = destinoPagamentoService;
  }

  public List<Pedido> listar(TipoPedido tipo) {
    return tipo == null ? repository.findAllDetalhado() : repository.findDetalhadoByTipo(tipo);
  }

  public Pedido buscar(Long id) {
    return repository.findDetalhadoById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
  }

  public Pedido criar(PedidoRequest request) {
    var pedido = new Pedido(tipo(request.tipo()), empresa(request.empresaId()), proximoNumero(tipo(request.tipo())),
        request.dataEmissao(), request.observacao());
    pedido.atualizarCabecalho(pedido.getEmpresa(), formaPagamento(request.formaPagamentoId()),
        transportadora(request.transportadoraId()), request.frete(), request.dataEmissao(), request.observacao());
    pedido.substituirItens(itens(request.itens()));
    return buscar(repository.save(pedido).getId());
  }

  public Pedido atualizar(Long id, PedidoRequest request) {
    var pedido = buscar(id);
    try {
      pedido.atualizarCabecalho(empresa(request.empresaId()), formaPagamento(request.formaPagamentoId()),
          transportadora(request.transportadoraId()), request.frete(), request.dataEmissao(), request.observacao());
      pedido.substituirItens(itens(request.itens()));
    } catch (IllegalStateException error) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, error.getMessage());
    }
    return buscar(repository.save(pedido).getId());
  }

  public Pedido faturar(Long id) {
    var pedido = buscar(id);
    if (pedido.getStatus() == StatusPedido.FATURADO) return pedido;
    var forma = pedido.getFormaPagamento();
    var dias = forma == null ? 30 : forma.getDiasVencimento();
    destinoPagamentoService.garantir(forma, dias == 0);
    try {
      pedido.faturar();
    } catch (IllegalStateException error) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, error.getMessage());
    }
    moverEstoque(pedido, 1);
    var tipoTitulo = pedido.getTipo() == TipoPedido.VENDA ? TipoTitulo.RECEBER : TipoTitulo.PAGAR;
    var titulo = new Titulo(tipoTitulo, pedido.getEmpresa(), pedido,
        (pedido.getTipo() == TipoPedido.VENDA ? "VENDA " : "COMPRA ") + pedido.getNumero(),
        pedido.getDataEmissao().plusDays(dias), pedido.getTotal());
    titulo.definirDestino(forma == null ? null : forma.getContaCorrente(), forma == null ? null : forma.getCaixa());
    if (dias == 0) {
      titulo.baixar();
      titulo = tituloRepository.save(titulo);
      destinoPagamentoService.disponibilizar(titulo);
    } else {
      tituloRepository.save(titulo);
    }
    repository.save(pedido);
    return buscar(pedido.getId());
  }

  public Pedido cancelar(Long id) {
    var pedido = buscar(id);
    if (pedido.getStatus() == StatusPedido.FATURADO) {
      var titulos = tituloRepository.findByPedido_Id(pedido.getId());
      if (titulos.stream().anyMatch(titulo -> titulo.getStatus() == StatusTitulo.LIQUIDADO))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Pedido possui título liquidado e não pode ser cancelado");
      titulos.forEach(titulo -> {
        if (titulo.getStatus() == StatusTitulo.ABERTO) titulo.cancelar();
      });
      moverEstoque(pedido, -1);
    }
    pedido.cancelar();
    return buscar(repository.save(pedido).getId());
  }

  public Pedido alterarAtivo(Long id, boolean ativo) {
    var pedido = buscar(id);
    pedido.alterarAtivo(ativo);
    return buscar(repository.save(pedido).getId());
  }

  public void excluir(Long id) {
    var pedido = buscar(id);
    if (pedido.getStatus() != StatusPedido.ABERTO)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente pedidos abertos podem ser excluídos");
    repository.delete(pedido);
  }

  private void moverEstoque(Pedido pedido, int sentido) {
    for (var item : pedido.getItens()) {
      var produto = produtoRepository.findById(item.getProduto().getId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
      var delta = item.getQuantidade().multiply(BigDecimal.valueOf(sentido * (pedido.getTipo() == TipoPedido.VENDA ? -1 : 1)));
      if (produto.getEstoque().add(delta).compareTo(BigDecimal.ZERO) < 0)
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Estoque insuficiente para " + produto.getSku());
      produto.ajustarEstoque(delta);
    }
  }

  private List<PedidoItem> itens(List<ItemRequest> itens) {
    if (itens == null || itens.isEmpty())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos um item");
    var lista = new ArrayList<PedidoItem>();
    for (var item : itens) {
      if (item.quantidade() == null || item.quantidade().compareTo(BigDecimal.ZERO) <= 0)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade deve ser maior que zero");
      var produto = produtoRepository.findById(item.produtoId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inválido"));
      if (!produto.isAtivo()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inativo: " + produto.getSku());
      var valor = item.valorUnitario() == null
          ? (produto.getPrecoVenda() == null ? BigDecimal.ZERO : produto.getPrecoVenda())
          : item.valorUnitario();
      lista.add(new PedidoItem(produto, item.quantidade(), valor));
    }
    return lista;
  }

  private Empresa empresa(Long id) {
    return empresaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empresa inválida"));
  }

  private FormaPagamento formaPagamento(Long id) {
    if (id == null) return null;
    var forma = formaPagamentoRepository.findComDestinoById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forma de pagamento inválida"));
    if (!forma.isAtivo()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forma de pagamento inativa");
    destinoPagamentoService.garantir(forma, false);
    return forma;
  }

  private Transportadora transportadora(Long id) {
    if (id == null) return null;
    var transportadora = transportadoraRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transportadora inválida"));
    if (!transportadora.isAtivo()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transportadora inativa");
    return transportadora;
  }

  private TipoPedido tipo(String tipo) {
    try {
      return TipoPedido.valueOf(tipo == null ? "VENDA" : tipo);
    } catch (IllegalArgumentException error) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de pedido inválido");
    }
  }

  private String proximoNumero(TipoPedido tipo) {
    int sequencia = java.util.Optional.ofNullable(repository.ultimoNumero(tipo.name())).orElse(0) + 1;
    return (tipo == TipoPedido.VENDA ? "VD" : "CP") + String.format("%06d", sequencia);
  }

  public record PedidoRequest(String tipo, Long empresaId, Long formaPagamentoId, Long transportadoraId,
      BigDecimal frete, LocalDate dataEmissao, String observacao, List<ItemRequest> itens) {}
  public record ItemRequest(Long produtoId, BigDecimal quantidade, BigDecimal valorUnitario) {}
}
