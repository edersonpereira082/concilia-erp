package br.com.conciliacao.web;

import br.com.conciliacao.domain.ContaCorrente;
import br.com.conciliacao.service.ContaCorrenteService;
import br.com.conciliacao.service.ContaCorrenteService.ContaCorrenteRequest;
import br.com.conciliacao.service.ContaCorrenteService.LancamentoRequest;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contas-correntes")
public class ContaCorrenteController {
  private final ContaCorrenteService service;
  public ContaCorrenteController(ContaCorrenteService service) { this.service = service; }

  @PostMapping
  public ResponseEntity<ContaCorrente> criar(@RequestBody ContaCorrenteRequest request) {
    var criada = service.criar(request);
    return ResponseEntity.created(Objects.requireNonNull(URI.create("/api/contas-correntes/" + criada.getId()))).body(criada);
  }

  @GetMapping public List<ContaCorrente> listar() { return service.listar(); }
  @GetMapping("/{id}") public ContaCorrente buscar(@PathVariable Long id) { return service.buscar(id); }
  @PostMapping("/{id}/lancamentos") public ContaCorrente lancar(@PathVariable Long id, @RequestBody LancamentoRequest request) {
    return service.lancar(id, request);
  }
  @PutMapping("/{id}") public ContaCorrente atualizar(@PathVariable Long id, @RequestBody ContaCorrenteRequest request) {
    return service.atualizar(id, request);
  }
  @PatchMapping("/{id}/ativo") public ContaCorrente alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }
  @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
  public record AtivoRequest(boolean ativo) {}
}
