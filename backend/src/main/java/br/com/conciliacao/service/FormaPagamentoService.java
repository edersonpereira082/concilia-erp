package br.com.conciliacao.service;

import br.com.conciliacao.domain.Caixa;
import br.com.conciliacao.domain.CaixaRepository;
import br.com.conciliacao.domain.ContaCorrente;
import br.com.conciliacao.domain.ContaCorrenteRepository;
import br.com.conciliacao.domain.FormaPagamento;
import br.com.conciliacao.domain.FormaPagamentoRepository;
import br.com.conciliacao.domain.TipoDestinoPagamento;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class FormaPagamentoService {
  private final FormaPagamentoRepository repository;
  private final ContaCorrenteRepository contaCorrenteRepository;
  private final CaixaRepository caixaRepository;

  public FormaPagamentoService(FormaPagamentoRepository repository, ContaCorrenteRepository contaCorrenteRepository,
      CaixaRepository caixaRepository) {
    this.repository = repository;
    this.contaCorrenteRepository = contaCorrenteRepository;
    this.caixaRepository = caixaRepository;
  }

  public FormaPagamento criar(FormaPagamentoRequest request) {
    var forma = from(request);
    if (repository.existsByNomeIgnoreCase(forma.getNome()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Forma de pagamento já cadastrada");
    return repository.save(forma);
  }

  public List<FormaPagamento> listar() { return repository.findAllComDestino(); }

  public FormaPagamento buscar(Long id) {
    return repository.findComDestinoById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Forma de pagamento não encontrada"));
  }

  public FormaPagamento atualizar(Long id, FormaPagamentoRequest request) {
    var forma = buscar(id);
    var dados = from(request);
    if (repository.findByNomeIgnoreCase(dados.getNome()).filter(encontrada -> !encontrada.getId().equals(id)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Forma de pagamento já cadastrada");
    forma.atualizar(dados.getNome(), dados.getParcelas(), dados.getDiasVencimento(), dados.getTipoDestino(),
        dados.getContaCorrente(), dados.getCaixa());
    return repository.save(forma);
  }

  public FormaPagamento alterarAtivo(Long id, boolean ativo) {
    var forma = buscar(id);
    forma.alterarAtivo(ativo);
    return repository.save(forma);
  }

  public void excluir(Long id) {
    try {
      repository.delete(buscar(id));
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Forma de pagamento possui pedidos vinculados", exception);
    }
  }

  private FormaPagamento from(FormaPagamentoRequest request) {
    try {
      return new FormaPagamento(request.nome(), request.parcelas() == null ? 1 : request.parcelas(),
          request.diasVencimento() == null ? 0 : request.diasVencimento(), destino(request.tipoDestino()),
          conta(request.contaCorrenteId()), caixa(request.caixaId()));
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
  }

  private TipoDestinoPagamento destino(String tipoDestino) {
    if (tipoDestino == null || tipoDestino.isBlank()) return TipoDestinoPagamento.CAIXA;
    try {
      return TipoDestinoPagamento.valueOf(tipoDestino);
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destino da forma de pagamento inválido");
    }
  }

  private ContaCorrente conta(Long id) {
    if (id == null) return null;
    var conta = contaCorrenteRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta corrente inválida"));
    if (!conta.isAtivo()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta corrente inativa");
    return conta;
  }

  private Caixa caixa(Long id) {
    if (id == null) return null;
    var caixa = caixaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caixa inválido"));
    if (!caixa.isAtivo()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caixa inativo");
    return caixa;
  }

  public record FormaPagamentoRequest(String nome, Integer parcelas, Integer diasVencimento, String tipoDestino,
      Long contaCorrenteId, Long caixaId) {}
}
