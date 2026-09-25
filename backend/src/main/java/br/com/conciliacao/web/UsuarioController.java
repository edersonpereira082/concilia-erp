package br.com.conciliacao.web;

import br.com.conciliacao.domain.Usuario;
import br.com.conciliacao.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
  private final AuthService service;
  public UsuarioController(AuthService service) { this.service = service; }

  @GetMapping
  public List<Usuario> listar(@RequestHeader("Authorization") String authorization) {
    return service.listarUsuarios(service.autenticar(token(authorization)));
  }

  @PostMapping
  public Usuario cadastrar(@RequestHeader("Authorization") String authorization,
      @Valid @RequestBody CadastroUsuarioRequest request) {
    return service.cadastrarUsuario(service.autenticar(token(authorization)),
        new AuthService.CadastroUsuario(request.nome(), request.email(), request.senha(), request.perfil()));
  }

  @PutMapping("/{id}")
  public Usuario atualizar(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
      @Valid @RequestBody AtualizarUsuarioRequest request) {
    return service.atualizarUsuario(service.autenticar(token(authorization)), id,
        new AuthService.CadastroUsuario(request.nome(), request.email(), request.senha(), request.perfil()));
  }

  @PatchMapping("/{id}/ativo")
  public Usuario alterarAtivo(@RequestHeader("Authorization") String authorization, @PathVariable Long id,
      @RequestBody AtivoRequest request) {
    return service.alterarAtivo(service.autenticar(token(authorization)), id, request.ativo());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
    service.excluirUsuario(service.autenticar(token(authorization)), id);
    return ResponseEntity.noContent().build();
  }

  private String token(String authorization) {
    return authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
  }

  public record CadastroUsuarioRequest(@NotBlank @Size(max = 120) String nome,
      @NotBlank @Email String email, @NotBlank @Size(min = 8, max = 72) String senha, String perfil) {}
  public record AtualizarUsuarioRequest(@NotBlank @Size(max = 120) String nome,
      @NotBlank @Email String email, String senha, String perfil) {}
  public record AtivoRequest(boolean ativo) {}
}