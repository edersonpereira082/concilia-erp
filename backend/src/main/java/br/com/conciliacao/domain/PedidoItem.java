package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido_item")
public class PedidoItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @JsonIgnore @ManyToOne(optional = false) @JoinColumn(name = "pedido_id") private Pedido pedido;
  @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "produto_id")
  @JsonIgnoreProperties({"precoCusto", "estoqueMinimo"})
  private Produto produto;
  @Column(nullable = false, precision = 15, scale = 3) private BigDecimal quantidade;
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal valorUnitario;
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal total;

  protected PedidoItem() {}

  public PedidoItem(Produto produto, BigDecimal quantidade, BigDecimal valorUnitario) {
    this.produto = produto;
    this.quantidade = Produto.quantidade(quantidade);
    this.valorUnitario = Produto.dinheiro(valorUnitario);
    this.total = this.quantidade.multiply(this.valorUnitario);
    this.total = Produto.dinheiro(this.total);
  }

  void vincular(Pedido pedido) { this.pedido = pedido; }

  public Long getId() { return id; }
  public Produto getProduto() { return produto; }
  public BigDecimal getQuantidade() { return quantidade; }
  public BigDecimal getValorUnitario() { return valorUnitario; }
  public BigDecimal getTotal() { return total; }
}
