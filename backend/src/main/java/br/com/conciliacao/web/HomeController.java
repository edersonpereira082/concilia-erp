package br.com.conciliacao.web;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
  private final URI frontend;

  public HomeController(@Value("${app.frontend-url:http://localhost:5173/}") String frontendUrl) {
    this.frontend = URI.create(frontendUrl);
  }

  @GetMapping("/")
  public ResponseEntity<Void> inicio() {
    return ResponseEntity.status(HttpStatus.FOUND).location(frontend).build();
  }
}
