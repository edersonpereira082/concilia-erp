package br.com.conciliacao.service;

import br.com.conciliacao.domain.Transportadora;
import br.com.conciliacao.domain.TransportadoraRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TransportadoraService {
  private final TransportadoraRepository repository;

  public TransportadoraService(TransportadoraRepository repository) { this.repository = repository; }

  public Transportadora criar(TransportadoraRequest request) {
    var transportadora = from(request);
    if (repository.existsByNomeIgnoreCase(transportadora.getNome()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Transportadora já cadastrada");
    return repository.save(transportadora);
  }

  public List<Transportadora> listar() { return repository.findAll(); }

  public Transportadora buscar(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transportadora não encontrada"));
  }

  public Transportadora atualizar(Long id, TransportadoraRequest request) {
    var transportadora = buscar(id);
    var dados = from(request);
    if (repository.findByNomeIgnoreCase(dados.getNome()).filter(encontrada -> !encontrada.getId().equals(id)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Transportadora já cadastrada");
    transportadora.atualizar(dados.getNome(), dados.getCnpj(), dados.getTelefone());
    return repository.save(transportadora);
  }

  public Transportadora alterarAtivo(Long id, boolean ativo) {
    var transportadora = buscar(id);
    transportadora.alterarAtivo(ativo);
    return repository.save(transportadora);
  }

  public void excluir(Long id) {
    try {
      repository.delete(buscar(id));
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Transportadora possui pedidos vinculados", exception);
    }
  }

  private Transportadora from(TransportadoraRequest request) {
    return new Transportadora(request.nome(), request.cnpj(), request.telefone());
  }

  public record TransportadoraRequest(String nome, String cnpj, String telefone) {}
}
