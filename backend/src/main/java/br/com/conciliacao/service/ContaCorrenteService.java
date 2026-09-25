package br.com.conciliacao.service;

import br.com.conciliacao.domain.ContaCorrente;
import br.com.conciliacao.domain.ContaCorrenteRepository;
import br.com.conciliacao.domain.ContaMovimento;
import br.com.conciliacao.domain.ContaMovimentoRepository;
import br.com.conciliacao.domain.FormaPagamentoRepository;
import br.com.conciliacao.domain.OrigemMovimentoCaixa;
import br.com.conciliacao.domain.TipoConta;
import br.com.conciliacao.domain.TipoDestinoPagamento;
import br.com.conciliacao.domain.TipoMovimentoCaixa;
import br.com.conciliacao.domain.TipoPedido;
import br.com.conciliacao.domain.TipoTitulo;
import br.com.conciliacao.domain.Titulo;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ContaCorrenteService {
  private final ContaCorrenteRepository repository;
  private final FormaPagamentoRepository formaPagamentoRepository;
  private final ContaMovimentoRepository movimentoRepository;

  public ContaCorrenteService(ContaCorrenteRepository repository, FormaPagamentoRepository formaPagamentoRepository,
      ContaMovimentoRepository movimentoRepository) {
    this.repository = repository;
    this.formaPagamentoRepository = formaPagamentoRepository;
    this.movimentoRepository = movimentoRepository;
  }

  public ContaCorrente criar(ContaCorrenteRequest request) {
    var conta = from(request);
    validarUnicidade(conta, null);
    return anexar(repository.save(conta));
  }

  public List<ContaCorrente> listar() {
    var movimentos = movimentoRepository.findAllDetalhado().stream()
        .collect(Collectors.groupingBy(movimento -> movimento.getContaCorrente().getId()));
    return repository.findAll().stream().map(conta -> anexar(conta, movimentos.getOrDefault(conta.getId(), List.of()))).toList();
  }

  public ContaCorrente buscar(Long id) {
    var conta = repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta corrente não encontrada"));
    return anexar(conta);
  }

  public ContaCorrente atualizar(Long id, ContaCorrenteRequest request) {
    var conta = buscar(id);
    var dados = from(request);
    validarUnicidade(dados, id);
    try {
      conta.atualizar(dados.getDescricao(), dados.getBancoCodigo(), dados.getBancoNome(), dados.getAgencia(),
          dados.getAgenciaDigito(), dados.getConta(), dados.getContaDigito(), dados.getTipo(), dados.getTitular(), dados.getPix());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
    return anexar(repository.save(conta));
  }

  public ContaCorrente alterarAtivo(Long id, boolean ativo) {
    var conta = buscar(id);
    conta.alterarAtivo(ativo);
    return anexar(repository.save(conta));
  }

  public void excluir(Long id) {
    if (formaPagamentoRepository.existsByContaCorrente_Id(id))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Conta corrente vinculada a forma de pagamento");
    if (movimentoRepository.existsByContaCorrente_Id(id))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Conta possui movimentação e não pode ser excluída");
    try {
      repository.delete(buscar(id));
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Conta corrente possui vínculos e não pode ser excluída", exception);
    }
  }

  public ContaCorrente lancar(Long id, LancamentoRequest request) {
    var conta = repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta corrente não encontrada"));
    if (!conta.isAtivo()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Conta inativa");
    var tipo = request.tipo() == TipoMovimentoCaixa.SAIDA ? TipoMovimentoCaixa.SAIDA : TipoMovimentoCaixa.ENTRADA;
    var descricao = request.descricao() == null || request.descricao().isBlank() ? "LANCAMENTO MANUAL" : request.descricao();
    try {
      movimentoRepository.save(new ContaMovimento(conta, tipo, OrigemMovimentoCaixa.LANCAMENTO, request.valor(), descricao, null, null));
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
    return buscar(id);
  }

  public void disponibilizar(Titulo titulo) {
    var pedido = titulo.getPedido();
    if (pedido == null || pedido.getFormaPagamento() == null) return;
    var forma = pedido.getFormaPagamento();
    if (forma.getTipoDestino() != TipoDestinoPagamento.CONTA_CORRENTE) return;
    if (movimentoRepository.existsByTitulo_Id(titulo.getId())) return;
    var conta = forma.getContaCorrente();
    if (conta == null || !conta.isAtivo())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forma de pagamento sem conta bancária ativa");
    var tipo = titulo.getTipo() == TipoTitulo.RECEBER ? TipoMovimentoCaixa.ENTRADA : TipoMovimentoCaixa.SAIDA;
    var origem = pedido.getTipo() == TipoPedido.VENDA ? OrigemMovimentoCaixa.VENDA : OrigemMovimentoCaixa.COMPRA;
    try {
      movimentoRepository.save(new ContaMovimento(conta, tipo, origem, titulo.getValor(), titulo.getDescricao(), titulo, pedido));
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
    }
  }

  private ContaCorrente anexar(ContaCorrente conta) {
    return anexar(conta, movimentoRepository.findByContaCorrente_IdOrderByIdDesc(conta.getId()));
  }

  private ContaCorrente anexar(ContaCorrente conta, List<ContaMovimento> movimentos) {
    var saldo = movimentos.stream()
        .map(movimento -> movimento.getTipo() == TipoMovimentoCaixa.ENTRADA ? movimento.getValor() : movimento.getValor().negate())
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2);
    conta.definirMovimentacao(saldo, movimentos);
    return conta;
  }

  private void validarUnicidade(ContaCorrente conta, Long idAtual) {
    if (repository.findByDescricaoIgnoreCase(conta.getDescricao()).filter(encontrada -> !encontrada.getId().equals(idAtual)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com essa descrição");
    if (repository.findByBancoCodigoAndAgenciaAndConta(conta.getBancoCodigo(), conta.getAgencia(), conta.getConta())
        .filter(encontrada -> !encontrada.getId().equals(idAtual)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Conta corrente já cadastrada para esse banco e agência");
  }

  private ContaCorrente from(ContaCorrenteRequest request) {
    try {
      return new ContaCorrente(request.descricao(), request.bancoCodigo(), request.bancoNome(), request.agencia(),
          request.agenciaDigito(), request.conta(), request.contaDigito(), request.tipo(), request.titular(), request.pix());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
  }

  public record ContaCorrenteRequest(String descricao, String bancoCodigo, String bancoNome, String agencia,
      String agenciaDigito, String conta, String contaDigito, TipoConta tipo, String titular, String pix) {}
  public record LancamentoRequest(TipoMovimentoCaixa tipo, String descricao, BigDecimal valor) {}
}
