package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conta_corrente", uniqueConstraints = @UniqueConstraint(name = "uk_conta_corrente_banco_agencia_conta", columnNames = {"banco_codigo", "agencia", "conta"}))
public class ContaCorrente {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, unique = true, length = 80) private String descricao;
  @Column(name = "banco_codigo", nullable = false, length = 3) private String bancoCodigo;
  @Column(name = "banco_nome", nullable = false, length = 120) private String bancoNome;
  @Column(nullable = false, length = 20) private String agencia;
  @Column(name = "agencia_digito", length = 2) private String agenciaDigito;
  @Column(nullable = false, length = 20) private String conta;
  @Column(name = "conta_digito", length = 2) private String contaDigito;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoConta tipo = TipoConta.CORRENTE;
  @Column(length = 150) private String titular;
  @Column(length = 120) private String pix;
  @Column(nullable = false) private boolean ativo = true;
  @Transient private BigDecimal saldoAtual = BigDecimal.ZERO.setScale(2);
  @Transient @JsonIgnoreProperties({"contaCorrente"})
  private List<ContaMovimento> movimentos = new ArrayList<>();

  protected ContaCorrente() {}

  public ContaCorrente(String descricao, String bancoCodigo, String bancoNome, String agencia, String agenciaDigito,
      String conta, String contaDigito, TipoConta tipo, String titular, String pix) {
    atualizar(descricao, bancoCodigo, bancoNome, agencia, agenciaDigito, conta, contaDigito, tipo, titular, pix);
  }

  public void atualizar(String descricao, String bancoCodigo, String bancoNome, String agencia, String agenciaDigito,
      String conta, String contaDigito, TipoConta tipo, String titular, String pix) {
    this.descricao = Texto.maiusculo(obrigatorio(descricao, "Descrição"));
    this.bancoCodigo = codigoBanco(bancoCodigo);
    this.bancoNome = Texto.maiusculo(obrigatorio(bancoNome, "Nome do banco"));
    this.agencia = limiteDigitos(obrigatorio(soDigitos(agencia), "Agência"), "Agência", 20);
    this.agenciaDigito = limiteDigitos(soDigitos(agenciaDigito), "Dígito da agência", 2);
    this.conta = limiteDigitos(obrigatorio(soDigitos(conta), "Conta"), "Conta", 20);
    this.contaDigito = limiteDigitos(soDigitos(contaDigito), "Dígito da conta", 2);
    this.tipo = tipo == null ? TipoConta.CORRENTE : tipo;
    this.titular = Texto.maiusculo(vazioParaNulo(titular));
    this.pix = chavePix(pix);
  }

  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }

  public void definirMovimentacao(BigDecimal saldoAtual, List<ContaMovimento> movimentos) {
    this.saldoAtual = saldoAtual == null ? BigDecimal.ZERO.setScale(2) : saldoAtual;
    this.movimentos = movimentos == null ? new ArrayList<>() : movimentos;
  }

  @Transient
  public String getContaResumo() {
    return bancoCodigo + " · AG " + formatar(agencia, agenciaDigito) + " · CC " + formatar(conta, contaDigito);
  }

  public Long getId() { return id; }
  public String getDescricao() { return descricao; }
  public String getBancoCodigo() { return bancoCodigo; }
  public String getBancoNome() { return bancoNome; }
  public String getAgencia() { return agencia; }
  public String getAgenciaDigito() { return agenciaDigito; }
  public String getConta() { return conta; }
  public String getContaDigito() { return contaDigito; }
  public TipoConta getTipo() { return tipo; }
  public String getTitular() { return titular; }
  public String getPix() { return pix; }
  public boolean isAtivo() { return ativo; }
  public BigDecimal getSaldoAtual() { return saldoAtual; }
  public List<ContaMovimento> getMovimentos() { return movimentos; }

  private static String codigoBanco(String valor) {
    var codigo = obrigatorio(soDigitos(valor), "Código do banco");
    if (codigo.length() > 3) throw new IllegalArgumentException("Código do banco deve ter até 3 dígitos");
    return "000".substring(codigo.length()) + codigo;
  }

  private static String chavePix(String valor) {
    var pix = vazioParaNulo(valor);
    if (pix == null) return null;
    return pix.contains("@") ? pix.trim() : Texto.maiusculo(pix);
  }

  private static String soDigitos(String valor) {
    if (valor == null) return null;
    var limpo = valor.replaceAll("\\D", "");
    return limpo.isBlank() ? null : limpo;
  }

  private static String limiteDigitos(String valor, String campo, int maximo) {
    if (valor != null && valor.length() > maximo)
      throw new IllegalArgumentException(campo + " deve ter até " + maximo + " dígitos");
    return valor;
  }

  private static String vazioParaNulo(String valor) {
    return valor == null || valor.isBlank() ? null : valor.trim();
  }

  private static String obrigatorio(String valor, String campo) {
    if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " é obrigatório");
    return valor.trim();
  }

  private static String formatar(String numero, String digito) {
    return digito == null || digito.isBlank() ? numero : numero + "-" + digito;
  }
}
