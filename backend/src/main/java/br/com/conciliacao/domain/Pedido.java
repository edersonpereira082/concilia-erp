package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoPedido tipo;
  @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "empresa_id")
  @JsonIgnoreProperties({"email", "telefone", "cidade", "uf"})
  private Empresa empresa;
  @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "forma_pagamento_id")
  private FormaPagamento formaPagamento;
  @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "transportadora_id")
  @JsonIgnoreProperties({"telefone"})
  private Transportadora transportadora;
  @Column(nullable = false, unique = true, length = 20) private String numero;
  @Column(nullable = false) private LocalDate dataEmissao;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StatusPedido status = StatusPedido.ABERTO;
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal frete = BigDecimal.ZERO;
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal total = BigDecimal.ZERO;
  @Column(length = 255) private String observacao;
  @Column(nullable = false) private boolean ativo = true;
  @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PedidoItem> itens = new ArrayList<>();

  protected Pedido() {}

  public Pedido(TipoPedido tipo, Empresa empresa, String numero, LocalDate dataEmissao, String observacao) {
    this.tipo = tipo;
    this.empresa = empresa;
    this.numero = numero;
    this.dataEmissao = dataEmissao == null ? LocalDate.now() : dataEmissao;
    this.observacao = Texto.maiusculo(observacao);
  }

  public void atualizarCabecalho(Empresa empresa, FormaPagamento formaPagamento, Transportadora transportadora,
      BigDecimal frete, LocalDate dataEmissao, String observacao) {
    garantirAberto();
    this.empresa = empresa;
    this.formaPagamento = formaPagamento;
    this.transportadora = transportadora;
    this.frete = Produto.dinheiro(frete);
    this.dataEmissao = dataEmissao == null ? this.dataEmissao : dataEmissao;
    this.observacao = Texto.maiusculo(observacao);
  }

  public void substituirItens(List<PedidoItem> novos) {
    garantirAberto();
    itens.clear();
    for (PedidoItem item : novos) adicionar(item);
    recalcular();
  }

  public void adicionar(PedidoItem item) {
    item.vincular(this);
    itens.add(item);
  }

  public void faturar() {
    garantirAberto();
    if (itens.isEmpty()) throw new IllegalStateException("Pedido sem itens");
    this.status = StatusPedido.FATURADO;
  }

  public void cancelar() {
    if (status == StatusPedido.CANCELADO) return;
    this.status = StatusPedido.CANCELADO;
  }

  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }
  public void garantirAberto() {
    if (status != StatusPedido.ABERTO) throw new IllegalStateException("Somente pedidos abertos podem ser alterados");
  }

  public void recalcular() {
    var itensTotal = itens.stream().map(PedidoItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    this.total = itensTotal.add(this.frete == null ? BigDecimal.ZERO : this.frete);
  }

  public Long getId() { return id; }
  public TipoPedido getTipo() { return tipo; }
  public Empresa getEmpresa() { return empresa; }
  public FormaPagamento getFormaPagamento() { return formaPagamento; }
  public Transportadora getTransportadora() { return transportadora; }
  public String getNumero() { return numero; }
  public LocalDate getDataEmissao() { return dataEmissao; }
  public StatusPedido getStatus() { return status; }
  public BigDecimal getFrete() { return frete; }
  public BigDecimal getTotal() { return total; }
  public String getObservacao() { return observacao; }
  public boolean isAtivo() { return ativo; }
  public List<PedidoItem> getItens() { return itens; }
}
