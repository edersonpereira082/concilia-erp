package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caixa_sessao")
public class CaixaSessao {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "caixa_id")
  @JsonIgnoreProperties({"sessaoAberta", "saldoAtual", "statusOperacao"})
  private Caixa caixa;
  @Column(name = "data_abertura", nullable = false) private LocalDateTime dataAbertura;
  @Column(name = "data_fechamento") private LocalDateTime dataFechamento;
  @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2) private BigDecimal saldoInicial;
  @Column(name = "saldo_informado", precision = 15, scale = 2) private BigDecimal saldoInformado;
  @Column(name = "saldo_final", precision = 15, scale = 2) private BigDecimal saldoFinal;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StatusCaixaSessao status = StatusCaixaSessao.ABERTO;
  @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "usuario_abertura_id")
  @JsonIgnoreProperties({"email", "perfil", "ativo"})
  private Usuario usuarioAbertura;
  @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "usuario_fechamento_id")
  @JsonIgnoreProperties({"email", "perfil", "ativo"})
  private Usuario usuarioFechamento;
  @OneToMany(mappedBy = "sessao", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  @JsonIgnoreProperties({"sessao"})
  private List<CaixaMovimento> movimentos = new ArrayList<>();

  protected CaixaSessao() {}

  public CaixaSessao(Caixa caixa, BigDecimal saldoInicial, Usuario usuario) {
    this.caixa = caixa;
    this.dataAbertura = LocalDateTime.now();
    this.saldoInicial = Produto.dinheiro(saldoInicial);
    if (this.saldoInicial.compareTo(BigDecimal.ZERO) < 0)
      throw new IllegalArgumentException("Saldo inicial não pode ser negativo");
    this.status = StatusCaixaSessao.ABERTO;
    this.usuarioAbertura = usuario;
  }

  public void fechar(BigDecimal saldoInformado, Usuario usuario) {
    if (status != StatusCaixaSessao.ABERTO) throw new IllegalStateException("Caixa já está fechado");
    this.dataFechamento = LocalDateTime.now();
    this.status = StatusCaixaSessao.FECHADO;
    this.usuarioFechamento = usuario;
    var calculado = getSaldoCalculado();
    this.saldoInformado = saldoInformado == null ? calculado : Produto.dinheiro(saldoInformado);
    this.saldoFinal = this.saldoInformado;
  }

  public void adicionar(CaixaMovimento movimento) {
    if (status != StatusCaixaSessao.ABERTO) throw new IllegalStateException("Caixa fechado");
    movimento.vincular(this);
    movimentos.add(movimento);
  }

  public BigDecimal getSaldoCalculado() {
    var entradas = movimentos.stream()
        .filter(item -> item.getTipo() == TipoMovimentoCaixa.ENTRADA)
        .map(CaixaMovimento::getValor)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    var saidas = movimentos.stream()
        .filter(item -> item.getTipo() == TipoMovimentoCaixa.SAIDA)
        .map(CaixaMovimento::getValor)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return Produto.dinheiro(saldoInicial.add(entradas).subtract(saidas));
  }

  public BigDecimal getDiferenca() {
    if (status != StatusCaixaSessao.FECHADO || saldoInformado == null) return null;
    return Produto.dinheiro(saldoInformado.subtract(getSaldoCalculado()));
  }

  public Long getId() { return id; }
  public Caixa getCaixa() { return caixa; }
  public LocalDateTime getDataAbertura() { return dataAbertura; }
  public LocalDateTime getDataFechamento() { return dataFechamento; }
  public BigDecimal getSaldoInicial() { return saldoInicial; }
  public BigDecimal getSaldoInformado() { return saldoInformado; }
  public BigDecimal getSaldoFinal() { return saldoFinal; }
  public StatusCaixaSessao getStatus() { return status; }
  public Usuario getUsuarioAbertura() { return usuarioAbertura; }
  public Usuario getUsuarioFechamento() { return usuarioFechamento; }
  public List<CaixaMovimento> getMovimentos() { return movimentos; }
}
