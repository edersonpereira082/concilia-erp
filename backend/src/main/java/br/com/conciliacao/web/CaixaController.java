package br.com.conciliacao.web;

import br.com.conciliacao.domain.Caixa;
import br.com.conciliacao.service.AuthService;
import br.com.conciliacao.service.CaixaService;
import br.com.conciliacao.service.CaixaService.AbrirRequest;
import br.com.conciliacao.service.CaixaService.CaixaRequest;
import br.com.conciliacao.service.CaixaService.FecharRequest;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caixas")
public class CaixaController {
  private final CaixaService service;
  private final AuthService authService;

  public CaixaController(CaixaService service, AuthService authService) {
    this.service = service;
    this.authService = authService;
  }

  @PostMapping
  public ResponseEntity<Caixa> criar(@RequestBody CaixaRequest request) {
    var criado = service.criar(request);
    return ResponseEntity.created(Objects.requireNonNull(URI.create("/api/caixas/" + criado.getId()))).body(criado);
  }

  @GetMapping public List<Caixa> listar() { return service.listar(); }
  @GetMapping("/{id}") public Caixa buscar(@PathVariable Long id) { return service.buscar(id); }
  @PutMapping("/{id}") public Caixa atualizar(@PathVariable Long id, @RequestBody CaixaRequest request) {
    return service.atualizar(id, request);
  }
  @PatchMapping("/{id}/ativo") public Caixa alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }
  @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/abrir")
  public Caixa abrir(@PathVariable Long id, @RequestHeader("Authorization") String authorization,
      @RequestBody(required = false) AbrirRequest request) {
    return service.abrir(id, authService.autenticar(token(authorization)),
        request == null ? null : request.saldoInicial());
  }

  @PostMapping("/{id}/fechar")
  public Caixa fechar(@PathVariable Long id, @RequestHeader("Authorization") String authorization,
      @RequestBody(required = false) FecharRequest request) {
    return service.fechar(id, authService.autenticar(token(authorization)),
        request == null ? null : request.saldoInformado());
  }

  private String token(String authorization) {
    return authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
  }

  public record AtivoRequest(boolean ativo) {}
}
