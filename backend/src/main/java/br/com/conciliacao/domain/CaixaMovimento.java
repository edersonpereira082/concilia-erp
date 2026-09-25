package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "caixa_movimento")
public class CaixaMovimento {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @JsonIgnore @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "sessao_id")
  private CaixaSessao sessao;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoMovimentoCaixa tipo;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private OrigemMovimentoCaixa origem;
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal valor;
  @Column(nullable = false, length = 180) private String descricao;
  @Column(name = "data_movimento", nullable = false) private LocalDateTime dataMovimento;
  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "titulo_id") private Titulo titulo;
  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pedido_id") private Pedido pedido;
  @Column(name = "pedido_numero", length = 20) private String pedidoNumero;

  protected CaixaMovimento() {}

  public CaixaMovimento(TipoMovimentoCaixa tipo, OrigemMovimentoCaixa origem, BigDecimal valor, String descricao,
      Titulo titulo, Pedido pedido) {
    this.tipo = tipo;
    this.origem = origem;
    this.valor = Produto.dinheiro(valor);
    if (this.valor.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalArgumentException("Valor do movimento deve ser maior que zero");
    this.descricao = Texto.maiusculo(descricao);
    this.dataMovimento = LocalDateTime.now();
    this.titulo = titulo;
    this.pedido = pedido;
    this.pedidoNumero = pedido == null ? null : pedido.getNumero();
  }

  public void vincular(CaixaSessao sessao) { this.sessao = sessao; }

  public Long getId() { return id; }
  public CaixaSessao getSessao() { return sessao; }
  public TipoMovimentoCaixa getTipo() { return tipo; }
  public OrigemMovimentoCaixa getOrigem() { return origem; }
  public BigDecimal getValor() { return valor; }
  public String getDescricao() { return descricao; }
  public LocalDateTime getDataMovimento() { return dataMovimento; }
  public Long getTituloId() { return titulo == null ? null : titulo.getId(); }
  public String getPedidoNumero() { return pedidoNumero; }
}
