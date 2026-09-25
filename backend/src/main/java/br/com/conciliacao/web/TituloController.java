package br.com.conciliacao.web;

import br.com.conciliacao.domain.Titulo;
import br.com.conciliacao.domain.TipoTitulo;
import br.com.conciliacao.service.TituloService;
import br.com.conciliacao.service.TituloService.TituloRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/titulos")
public class TituloController {
  private final TituloService service;
  public TituloController(TituloService service) { this.service = service; }

  @GetMapping public List<Titulo> listar(@RequestParam(required = false) TipoTitulo tipo) { return service.listar(tipo); }
  @PostMapping public Titulo criar(@RequestBody TituloRequest request) { return service.criar(request); }
  @PutMapping("/{id}") public Titulo atualizar(@PathVariable Long id, @RequestBody TituloRequest request) {
    return service.atualizar(id, request);
  }
  @PostMapping("/{id}/baixar") public Titulo baixar(@PathVariable Long id) { return service.baixar(id); }
  @PatchMapping("/{id}/ativo") public Titulo alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }
  @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
  public record AtivoRequest(boolean ativo) {}
}
