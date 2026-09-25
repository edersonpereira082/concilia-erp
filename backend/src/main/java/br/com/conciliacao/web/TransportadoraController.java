package br.com.conciliacao.web;

import br.com.conciliacao.domain.Transportadora;
import br.com.conciliacao.service.TransportadoraService;
import br.com.conciliacao.service.TransportadoraService.TransportadoraRequest;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transportadoras")
public class TransportadoraController {
  private final TransportadoraService service;
  public TransportadoraController(TransportadoraService service) { this.service = service; }

  @PostMapping
  public ResponseEntity<Transportadora> criar(@RequestBody TransportadoraRequest request) {
    var criada = service.criar(request);
    return ResponseEntity.created(Objects.requireNonNull(URI.create("/api/transportadoras/" + criada.getId()))).body(criada);
  }

  @GetMapping public List<Transportadora> listar() { return service.listar(); }
  @PutMapping("/{id}") public Transportadora atualizar(@PathVariable Long id, @RequestBody TransportadoraRequest request) {
    return service.atualizar(id, request);
  }
  @PatchMapping("/{id}/ativo") public Transportadora alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }
  @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
  public record AtivoRequest(boolean ativo) {}
}
