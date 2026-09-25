package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "titulo")
public class Titulo {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoTitulo tipo;
  @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "empresa_id")
  @JsonIgnoreProperties({"email", "telefone", "cidade", "uf"})
  private Empresa empresa;
  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pedido_id")
  private Pedido pedido;
  @Column(name = "pedido_numero", length = 20) private String pedidoNumero;
  @Column(nullable = false, length = 180) private String descricao;
  @Column(nullable = false) private LocalDate vencimento;
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal valor;
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal saldo;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StatusTitulo status = StatusTitulo.ABERTO;
  @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "conta_corrente_id")
  @JsonIgnoreProperties({"bancoCodigo", "bancoNome", "agencia", "agenciaDigito", "conta", "contaDigito", "titular", "pix", "movimentos"})
  private ContaCorrente contaCorrente;
  @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "caixa_id")
  @JsonIgnoreProperties({"sessaoAberta", "saldoAtual", "statusOperacao"})
  private Caixa caixa;
  @Column(nullable = false) private boolean ativo = true;

  protected Titulo() {}

  public Titulo(TipoTitulo tipo, Empresa empresa, Pedido pedido, String descricao, LocalDate vencimento, BigDecimal valor) {
    this.tipo = tipo;
    this.empresa = empresa;
    this.pedido = pedido;
    this.pedidoNumero = pedido == null ? null : pedido.getNumero();
    atualizar(descricao, vencimento, valor);
  }

  public void atualizar(String descricao, LocalDate vencimento, BigDecimal valor) {
    if (status != StatusTitulo.ABERTO) throw new IllegalStateException("Somente títulos abertos podem ser alterados");
    this.descricao = Texto.maiusculo(descricao);
    this.vencimento = vencimento;
    this.valor = Produto.dinheiro(valor);
    this.saldo = this.valor;
  }

  public void baixar() {
    if (status != StatusTitulo.ABERTO) throw new IllegalStateException("Somente títulos abertos podem ser baixados");
    this.saldo = BigDecimal.ZERO.setScale(2);
    this.status = StatusTitulo.LIQUIDADO;
  }

  public void cancelar() {
    if (status == StatusTitulo.LIQUIDADO) throw new IllegalStateException("Título liquidado não pode ser cancelado");
    this.status = StatusTitulo.CANCELADO;
    this.saldo = BigDecimal.ZERO.setScale(2);
  }

  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }

  public void definirDestino(ContaCorrente contaCorrente, Caixa caixa) {
    this.contaCorrente = contaCorrente;
    this.caixa = caixa;
  }

  public Long getId() { return id; }
  public TipoTitulo getTipo() { return tipo; }
  public Empresa getEmpresa() { return empresa; }
  @JsonIgnore public Pedido getPedido() { return pedido; }
  public Long getPedidoId() { return pedido == null ? null : pedido.getId(); }
  public String getPedidoNumero() { return pedidoNumero; }
  public String getDescricao() { return descricao; }
  public LocalDate getVencimento() { return vencimento; }
  public BigDecimal getValor() { return valor; }
  public BigDecimal getSaldo() { return saldo; }
  public StatusTitulo getStatus() { return status; }
  public ContaCorrente getContaCorrente() { return contaCorrente; }
  public Caixa getCaixa() { return caixa; }
  public boolean isAtivo() { return ativo; }

  public String getDestinoTipo() {
    if (contaCorrente != null) return "BANCO";
    if (caixa != null) return "CAIXA";
    return null;
  }

  public String getDestinoNome() {
    if (contaCorrente != null) {
      var tipo = contaCorrente.getTipo() == TipoConta.POUPANCA ? "POUPANÇA" : "CORRENTE";
      return contaCorrente.getDescricao() + " · " + tipo;
    }
    return caixa == null ? null : caixa.getDescricao();
  }
}
