package br.com.conciliacao.web;

import br.com.conciliacao.domain.SistemaLog;
import br.com.conciliacao.service.AuthService;
import br.com.conciliacao.service.SistemaLogService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class SistemaLogController {
  private final AuthService authService;
  private final SistemaLogService service;

  public SistemaLogController(AuthService authService, SistemaLogService service) {
    this.authService = authService;
    this.service = service;
  }

  @GetMapping
  public List<SistemaLog> listar(@RequestHeader("Authorization") String authorization) {
    var token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
    return service.listar(authService.autenticar(token));
  }
}
