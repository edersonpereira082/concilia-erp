package br.com.conciliacao.web;

import br.com.conciliacao.domain.Pedido;
import br.com.conciliacao.domain.TipoPedido;
import br.com.conciliacao.service.PedidoService;
import br.com.conciliacao.service.PedidoService.PedidoRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
  private final PedidoService service;
  public PedidoController(PedidoService service) { this.service = service; }

  @GetMapping public List<Pedido> listar(@RequestParam(required = false) TipoPedido tipo) { return service.listar(tipo); }
  @GetMapping("/{id}") public Pedido buscar(@PathVariable Long id) { return service.buscar(id); }
  @PostMapping public Pedido criar(@RequestBody PedidoRequest request) { return service.criar(request); }
  @PutMapping("/{id}") public Pedido atualizar(@PathVariable Long id, @RequestBody PedidoRequest request) {
    return service.atualizar(id, request);
  }
  @PostMapping("/{id}/faturar") public Pedido faturar(@PathVariable Long id) { return service.faturar(id); }
  @PostMapping("/{id}/cancelar") public Pedido cancelar(@PathVariable Long id) { return service.cancelar(id); }
  @PatchMapping("/{id}/ativo") public Pedido alterarAtivo(@PathVariable Long id, @RequestBody AtivoRequest request) {
    return service.alterarAtivo(id, request.ativo());
  }
  @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.excluir(id);
    return ResponseEntity.noContent().build();
  }
  public record AtivoRequest(boolean ativo) {}
}
