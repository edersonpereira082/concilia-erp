package br.com.conciliacao.service;

import br.com.conciliacao.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TituloService {
  private final TituloRepository repository;
  private final EmpresaRepository empresaRepository;
  private final DestinoPagamentoService destinoPagamentoService;

  public TituloService(TituloRepository repository, EmpresaRepository empresaRepository,
      DestinoPagamentoService destinoPagamentoService) {
    this.repository = repository;
    this.empresaRepository = empresaRepository;
    this.destinoPagamentoService = destinoPagamentoService;
  }

  public List<Titulo> listar(TipoTitulo tipo) {
    return tipo == null ? repository.findAllByOrderByVencimentoAsc() : repository.findByTipoOrderByVencimentoAsc(tipo);
  }

  public Titulo buscar(Long id) {
    return repository.findDetalhadoById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Título não encontrado"));
  }

  public Titulo criar(TituloRequest request) {
    var empresa = empresa(request.empresaId());
    var tipo = tipo(request.tipo());
    return repository.save(new Titulo(tipo, empresa, null, request.descricao(), request.vencimento(), request.valor()));
  }

  public Titulo atualizar(Long id, TituloRequest request) {
    var titulo = buscar(id);
    try {
      titulo.atualizar(request.descricao(), request.vencimento(), request.valor());
    } catch (IllegalStateException error) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, error.getMessage());
    }
    return repository.save(titulo);
  }

  public Titulo baixar(Long id) {
    var titulo = buscar(id);
    try {
      titulo.baixar();
    } catch (IllegalStateException error) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, error.getMessage());
    }
    destinoPagamentoService.disponibilizar(titulo);
    return repository.save(titulo);
  }

  public Titulo alterarAtivo(Long id, boolean ativo) {
    var titulo = buscar(id);
    titulo.alterarAtivo(ativo);
    return repository.save(titulo);
  }

  public void excluir(Long id) {
    var titulo = buscar(id);
    if (titulo.getPedido() != null)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Título gerado por pedido. Cancele o pedido para estornar.");
    if (titulo.getStatus() != StatusTitulo.ABERTO)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Somente títulos avulsos em aberto podem ser excluídos");
    repository.delete(titulo);
  }

  private Empresa empresa(Long id) {
    return empresaRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empresa inválida"));
  }

  private TipoTitulo tipo(String tipo) {
    try {
      return TipoTitulo.valueOf(tipo == null ? "RECEBER" : tipo);
    } catch (IllegalArgumentException error) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de título inválido");
    }
  }

  public record TituloRequest(String tipo, Long empresaId, String descricao, LocalDate vencimento, BigDecimal valor) {}
}
