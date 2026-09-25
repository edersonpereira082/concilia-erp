package br.com.conciliacao.config;

import br.com.conciliacao.domain.Usuario;
import br.com.conciliacao.service.AuthService;
import br.com.conciliacao.service.SistemaLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
  private final AuthService authService;
  private final SistemaLogService logService;

  public AuthInterceptor(AuthService authService, SistemaLogService logService) {
    this.authService = authService;
    this.logService = logService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getRequestURI().startsWith("/api/auth/")) return true;
    var authorization = request.getHeader("Authorization");
    var token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
    var usuario = authService.autenticar(token);
    request.setAttribute("authUsuario", usuario);
    if (exigeConfirmacao(request)) {
      var confirmador = authService.confirmarUsuario(usuario,
          request.getHeader("X-Confirm-Email"), request.getHeader("X-Confirm-Senha"));
      request.setAttribute("confirmador", confirmador);
    }
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
    if (exception != null || response.getStatus() >= 400) return;
    var method = request.getMethod();
    if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) return;
    var path = request.getRequestURI();
    if (path.startsWith("/api/logs") || path.startsWith("/api/auth/")) return;
    var usuario = (Usuario) request.getAttribute("authUsuario");
    var confirmador = (Usuario) request.getAttribute("confirmador");
    try {
      logService.registrar(usuario, confirmador, method, path, response.getStatus());
    } catch (Exception ignored) {
    }
  }

  private boolean exigeConfirmacao(HttpServletRequest request) {
    var method = request.getMethod();
    if ("PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method)) return true;
    if (!"POST".equals(method)) return false;
    var path = request.getRequestURI();
    return path.contains("/faturar") || path.contains("/cancelar") || path.contains("/baixar")
        || path.contains("/lancamentos") || path.contains("/abrir") || path.contains("/fechar");
  }
}
