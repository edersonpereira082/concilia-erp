package br.com.conciliacao.web;

import br.com.conciliacao.domain.Empresa;
import br.com.conciliacao.domain.TipoEmpresa;
import br.com.conciliacao.service.EmpresaService;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {
  private final EmpresaService service;

  public EmpresaController(EmpresaService service) { this.service = service; }

  @PostMapping
  public ResponseEntity<Empresa> criar(@RequestBody Empresa.Dados dados) {
    Empresa criada = service.criar(dados);
    return ResponseEntity.created(Objects.requireNonNull(URI.create("/api/empresas/" + criada.getId()))).body(criada);
  }

  @GetMapping
  public List<Empresa> listar(@RequestParam(required = false) TipoEmpresa tipo) {
    return service.listar(tipo);
  }

  @GetMapping("/{id}")
  public Empresa buscar(@PathVariable Long id) { return service.buscar(id); }

  @PutMapping("/{id}")
  public Empresa atualizar(@PathVariable Long id, @RequestBody Empresa.Dados dados) {
    return service.atualizar(id, dados);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/ativo")
  public Empresa alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }

  public record AtivoRequest(boolean ativo) {}
}
