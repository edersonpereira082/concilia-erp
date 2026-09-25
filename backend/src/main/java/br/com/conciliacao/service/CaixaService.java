package br.com.conciliacao.service;

import br.com.conciliacao.domain.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CaixaService {
  private final CaixaRepository repository;
  private final CaixaSessaoRepository sessaoRepository;
  private final CaixaMovimentoRepository movimentoRepository;
  private final FormaPagamentoRepository formaPagamentoRepository;

  public CaixaService(CaixaRepository repository, CaixaSessaoRepository sessaoRepository,
      CaixaMovimentoRepository movimentoRepository, FormaPagamentoRepository formaPagamentoRepository) {
    this.repository = repository;
    this.sessaoRepository = sessaoRepository;
    this.movimentoRepository = movimentoRepository;
    this.formaPagamentoRepository = formaPagamentoRepository;
  }

  public Caixa criar(CaixaRequest request) {
    var caixa = from(request);
    if (repository.existsByDescricaoIgnoreCase(caixa.getDescricao()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Caixa já cadastrado");
    return anexarSessao(repository.save(caixa));
  }

  public List<Caixa> listar() {
    var abertas = sessaoRepository.findDetalhadoByStatus(StatusCaixaSessao.ABERTO).stream()
        .collect(Collectors.toMap(sessao -> sessao.getCaixa().getId(), Function.identity(), (a, b) -> a));
    return repository.findAll().stream().map(caixa -> anexar(caixa, abertas)).toList();
  }

  public Caixa buscar(Long id) {
    var caixa = repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caixa não encontrado"));
    return anexarSessao(caixa);
  }

  public Caixa atualizar(Long id, CaixaRequest request) {
    var caixa = buscar(id);
    var dados = from(request);
    if (repository.findByDescricaoIgnoreCase(dados.getDescricao()).filter(encontrado -> !encontrado.getId().equals(id)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Caixa já cadastrado");
    try {
      caixa.atualizar(dados.getDescricao());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
    return anexarSessao(repository.save(caixa));
  }

  public Caixa alterarAtivo(Long id, boolean ativo) {
    var caixa = buscar(id);
    if (!ativo && caixa.getSessaoAberta() != null)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Feche o caixa antes de desativar");
    caixa.alterarAtivo(ativo);
    return anexarSessao(repository.save(caixa));
  }

  public void excluir(Long id) {
    var caixa = buscar(id);
    if (caixa.getSessaoAberta() != null)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Feche o caixa antes de excluir");
    if (formaPagamentoRepository.existsByCaixa_Id(id))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Caixa vinculado a forma de pagamento");
    if (sessaoRepository.existsByCaixa_Id(id))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Caixa possui movimentação e não pode ser excluído");
    try {
      repository.delete(caixa);
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Caixa possui vínculos e não pode ser excluído", exception);
    }
  }

  public Caixa abrir(Long id, Usuario usuario, BigDecimal saldoInicial) {
    var caixa = buscar(id);
    if (!caixa.isAtivo()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Caixa inativo");
    if (caixa.getSessaoAberta() != null)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Caixa já está aberto");
    try {
      sessaoRepository.save(new CaixaSessao(caixa, saldoInicial, usuario));
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
    return buscar(id);
  }

  public Caixa fechar(Long id, Usuario usuario, BigDecimal saldoInformado) {
    var caixa = buscar(id);
    var sessao = caixa.getSessaoAberta();
    if (sessao == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Caixa já está fechado");
    try {
      sessao.fechar(saldoInformado, usuario);
    } catch (IllegalStateException | IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
    }
    sessaoRepository.save(sessao);
    return buscar(id);
  }

  public void garantirAberto(Caixa caixa) {
    if (caixa == null || !caixa.isAtivo())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forma de pagamento sem caixa ativo");
    sessaoRepository.findByCaixa_IdAndStatus(caixa.getId(), StatusCaixaSessao.ABERTO)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
            "Abra o caixa " + caixa.getDescricao() + " para disponibilizar o valor"));
  }

  public void disponibilizar(Titulo titulo) {
    var pedido = titulo.getPedido();
    if (pedido == null || pedido.getFormaPagamento() == null) return;
    var forma = pedido.getFormaPagamento();
    if (forma.getTipoDestino() != TipoDestinoPagamento.CAIXA) return;
    if (movimentoRepository.existsByTitulo_Id(titulo.getId())) return;
    var caixa = forma.getCaixa();
    garantirAberto(caixa);
    var sessao = sessaoRepository.findDetalhadoByCaixaIdAndStatus(caixa.getId(), StatusCaixaSessao.ABERTO)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
            "Abra o caixa " + caixa.getDescricao() + " para disponibilizar o valor"));
    var tipo = titulo.getTipo() == TipoTitulo.RECEBER ? TipoMovimentoCaixa.ENTRADA : TipoMovimentoCaixa.SAIDA;
    var origem = pedido.getTipo() == TipoPedido.VENDA ? OrigemMovimentoCaixa.VENDA : OrigemMovimentoCaixa.COMPRA;
    try {
      sessao.adicionar(new CaixaMovimento(tipo, origem, titulo.getValor(), titulo.getDescricao(), titulo, pedido));
    } catch (IllegalArgumentException | IllegalStateException exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
    }
    sessaoRepository.save(sessao);
  }

  private Caixa anexarSessao(Caixa caixa) {
    sessaoRepository.findDetalhadoByCaixaIdAndStatus(caixa.getId(), StatusCaixaSessao.ABERTO)
        .ifPresent(caixa::definirSessaoAberta);
    return caixa;
  }

  private Caixa anexar(Caixa caixa, Map<Long, CaixaSessao> abertas) {
    caixa.definirSessaoAberta(abertas.get(caixa.getId()));
    return caixa;
  }

  private Caixa from(CaixaRequest request) {
    try {
      return new Caixa(request.descricao());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
  }

  public record CaixaRequest(String descricao) {}
  public record AbrirRequest(BigDecimal saldoInicial) {}
  public record FecharRequest(BigDecimal saldoInformado) {}
}
