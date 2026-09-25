package br.com.conciliacao.web;

import br.com.conciliacao.service.BoletoReportService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController @RequestMapping("/api/boletos")
public class BoletoController {
  private final BoletoReportService reports;
  public BoletoController(BoletoReportService reports) { this.reports = reports; }
  @GetMapping(value = "/exemplo/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> pdf() throws Exception { return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=boleto.pdf").body(reports.gerar("Empresa Demonstracao", "249,90", LocalDate.now().plusDays(10))); }
}
