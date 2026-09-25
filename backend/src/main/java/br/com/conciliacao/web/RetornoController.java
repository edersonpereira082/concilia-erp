package br.com.conciliacao.web;

import br.com.conciliacao.domain.RetornoBancario;
import br.com.conciliacao.service.RetornoService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController @RequestMapping("/api/retornos")
public class RetornoController {
  private final RetornoService service;
  public RetornoController(RetornoService service) { this.service = service; }
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public RetornoBancario receber(@RequestPart @NotNull MultipartFile arquivo) throws IOException { return service.receber(arquivo); }
  @GetMapping public List<RetornoBancario> listar() { return service.listar(); }
  @GetMapping("/{id}") public RetornoBancario buscar(@PathVariable Long id) { return service.buscar(id); }
  @GetMapping("/{id}/detalhes") public List<br.com.conciliacao.domain.RetornoDetalhe> detalhes(@PathVariable Long id) { return service.listarDetalhes(id); }
  @PutMapping("/{id}")
  public RetornoBancario atualizar(@PathVariable Long id, @RequestBody AtualizarRetornoRequest request) {
    return service.atualizar(id, request.nomeArquivo());
  }
  @PatchMapping("/{id}/ativo")
  public RetornoBancario alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
  public record AtualizarRetornoRequest(String nomeArquivo) {}
  public record AtivoRequest(boolean ativo) {}
}
