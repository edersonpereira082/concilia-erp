package br.com.conciliacao.web;

import br.com.conciliacao.domain.Produto;
import br.com.conciliacao.service.ProdutoService;
import br.com.conciliacao.service.ProdutoService.ProdutoRequest;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
  private final ProdutoService service;
  public ProdutoController(ProdutoService service) { this.service = service; }

  @PostMapping
  public ResponseEntity<Produto> criar(@RequestBody ProdutoRequest request) {
    Produto criado = service.criar(service.from(request));
    return ResponseEntity.created(Objects.requireNonNull(URI.create("/api/produtos/" + criado.getId()))).body(criado);
  }

  @GetMapping public List<Produto> listar() { return service.listar(); }
  @GetMapping("/{id}") public Produto buscar(@PathVariable Long id) { return service.buscar(id); }
  @PutMapping("/{id}") public Produto atualizar(@PathVariable Long id, @RequestBody ProdutoRequest request) {
    return service.atualizar(id, service.from(request));
  }
  @PatchMapping("/{id}/ativo") public Produto alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }
  @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
  public record AtivoRequest(boolean ativo) {}
}
