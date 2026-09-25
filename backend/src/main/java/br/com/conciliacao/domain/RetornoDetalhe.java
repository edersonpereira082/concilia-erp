package br.com.conciliacao.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "retorno_detalhe")
public class RetornoDetalhe {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "retorno_id", nullable = false)
  private RetornoBancario retorno;

  @Column(nullable = false) private int numeroLinha;
  @Column(nullable = false, length = 10) private String layout;
  @Column(nullable = false, length = 400) private String linha;
  @Column(length = 25) private String nossoNumero;
  @Column(length = 3) private String codigoOcorrencia;
  @Column(precision = 15, scale = 2) private BigDecimal valor;
  private LocalDate dataOcorrencia;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
  private ResultadoConciliacao resultado;
  @Column(length = 120) private String motivo;

  protected RetornoDetalhe() {}

  public RetornoDetalhe(RetornoBancario retorno, int numeroLinha, String layout, String linha,
      String nossoNumero, String codigoOcorrencia, BigDecimal valor, LocalDate dataOcorrencia,
      ResultadoConciliacao resultado, String motivo) {
    this.retorno = retorno;
    this.numeroLinha = numeroLinha;
    this.layout = layout;
    this.linha = linha;
    this.nossoNumero = nossoNumero;
    this.codigoOcorrencia = codigoOcorrencia;
    this.valor = valor;
    this.dataOcorrencia = dataOcorrencia;
    this.resultado = resultado;
    this.motivo = motivo;
  }

  public Long getId() { return id; }
  public int getNumeroLinha() { return numeroLinha; }
  public String getLayout() { return layout; }
  public String getLinha() { return linha; }
  public String getNossoNumero() { return nossoNumero; }
  public String getCodigoOcorrencia() { return codigoOcorrencia; }
  public BigDecimal getValor() { return valor; }
  public LocalDate getDataOcorrencia() { return dataOcorrencia; }
  public ResultadoConciliacao getResultado() { return resultado; }
  public String getMotivo() { return motivo; }
}
