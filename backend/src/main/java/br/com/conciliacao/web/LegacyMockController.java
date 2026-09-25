package br.com.conciliacao.web;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/mock-legado")
public class LegacyMockController {
  @GetMapping("/boletos/{nossoNumero}")
  public Map<String, Object> consultar(@PathVariable String nossoNumero) { return Map.of("nossoNumero", nossoNumero, "origem", "ORACLE_LEGACY_API", "situacao", "ABERTO"); }
}
