package br.com.conciliacao.web;

import br.com.conciliacao.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;
  public AuthController(AuthService service) { this.service = service; }

  @PostMapping("/login")
  public AuthService.ResultadoAuth login(@Valid @RequestBody LoginRequest request) {
    return service.entrar(request.email(), request.senha());
  }

  @PostMapping("/cadastro")
  public ResponseEntity<AuthService.ResultadoAuth> cadastro(@Valid @RequestBody CadastroRequest request) {
    var resultado = service.cadastrar(new AuthService.Cadastro(request.nome(), request.email(), request.senha(),
        request.razaoSocial(), request.nomeFantasia(), request.cnpj(), request.emailEmpresa(), request.telefone(),
        request.cidade(), request.uf()));
    return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
  }

  @GetMapping("/me")
  public AuthService.ResultadoAuth me(@RequestHeader(value = "Authorization", required = false) String authorization) {
    var usuario = service.autenticar(token(authorization));
    return new AuthService.ResultadoAuth(token(authorization), usuario.getId(), usuario.getNome(), usuario.getEmail(),
        usuario.getPerfil(), usuario.getSoftwareHouse().getNomeFantasia());
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
    service.sair(token(authorization));
    return ResponseEntity.noContent().build();
  }

  private String token(String authorization) {
    return authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
  }

  public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {}
  public record CadastroRequest(@NotBlank @Size(max = 120) String nome, @NotBlank @Email String email,
      @NotBlank @Size(min = 8, max = 72) String senha, @NotBlank @Size(max = 150) String razaoSocial,
      @Size(max = 150) String nomeFantasia, @NotBlank @Size(min = 14, max = 14) String cnpj,
      @NotBlank @Email String emailEmpresa, String telefone, String cidade, @Size(max = 2) String uf) {}
}