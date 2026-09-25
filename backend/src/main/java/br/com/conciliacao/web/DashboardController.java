package br.com.conciliacao.web;

import br.com.conciliacao.domain.RetornoBancario;
import br.com.conciliacao.domain.StatusPedido;
import br.com.conciliacao.domain.StatusTitulo;
import br.com.conciliacao.domain.TipoPedido;
import br.com.conciliacao.domain.TipoTitulo;
import br.com.conciliacao.service.PedidoService;
import br.com.conciliacao.service.ProdutoService;
import br.com.conciliacao.service.RetornoService;
import br.com.conciliacao.service.TituloService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final RetornoService retornos;
  private final PedidoService pedidos;
  private final TituloService titulos;
  private final ProdutoService produtos;

  public DashboardController(RetornoService retornos, PedidoService pedidos, TituloService titulos, ProdutoService produtos) {
    this.retornos = retornos;
    this.pedidos = pedidos;
    this.titulos = titulos;
    this.produtos = produtos;
  }

  @GetMapping
  public Map<String, Object> resumo() {
    var ativos = retornos.listar().stream().filter(RetornoBancario::isAtivo).toList();
    var mes = LocalDate.now().withDayOfMonth(1);
    var vendasMes = pedidos.listar(TipoPedido.VENDA).stream()
        .filter(pedido -> pedido.getStatus() == StatusPedido.FATURADO && !pedido.getDataEmissao().isBefore(mes))
        .map(pedido -> pedido.getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
    var aReceber = titulos.listar(TipoTitulo.RECEBER).stream()
        .filter(titulo -> titulo.isAtivo() && titulo.getStatus() == StatusTitulo.ABERTO)
        .map(titulo -> titulo.getSaldo()).reduce(BigDecimal.ZERO, BigDecimal::add);
    var aPagar = titulos.listar(TipoTitulo.PAGAR).stream()
        .filter(titulo -> titulo.isAtivo() && titulo.getStatus() == StatusTitulo.ABERTO)
        .map(titulo -> titulo.getSaldo()).reduce(BigDecimal.ZERO, BigDecimal::add);
    var estoqueBaixo = produtos.listar().stream().filter(produto -> produto.isAtivo() && produto.isEstoqueBaixo()).count();
    Map<String, Object> dados = new LinkedHashMap<>();
    dados.put("faturamentoMes", vendasMes);
    dados.put("aReceber", aReceber);
    dados.put("aPagar", aPagar);
    dados.put("estoqueBaixo", estoqueBaixo);
    dados.put("totalArquivos", ativos.size());
    dados.put("processados", ativos.stream().filter(r -> r.getStatus().name().equals("PROCESSADO")).count());
    dados.put("registros", ativos.stream().mapToInt(RetornoBancario::getRegistros).sum());
    return dados;
  }
}
