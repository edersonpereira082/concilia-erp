package br.com.conciliacao.web;

import br.com.conciliacao.domain.FormaPagamento;
import br.com.conciliacao.service.FormaPagamentoService;
import br.com.conciliacao.service.FormaPagamentoService.FormaPagamentoRequest;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/formas-pagamento")
public class FormaPagamentoController {
  private final FormaPagamentoService service;
  public FormaPagamentoController(FormaPagamentoService service) { this.service = service; }

  @PostMapping
  public ResponseEntity<FormaPagamento> criar(@RequestBody FormaPagamentoRequest request) {
    var criada = service.criar(request);
    return ResponseEntity.created(Objects.requireNonNull(URI.create("/api/formas-pagamento/" + criada.getId()))).body(criada);
  }

  @GetMapping public List<FormaPagamento> listar() { return service.listar(); }
  @PutMapping("/{id}") public FormaPagamento atualizar(@PathVariable Long id, @RequestBody FormaPagamentoRequest request) {
    return service.atualizar(id, request);
  }
  @PatchMapping("/{id}/ativo") public FormaPagamento alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }
  @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
  public record AtivoRequest(boolean ativo) {}
}
