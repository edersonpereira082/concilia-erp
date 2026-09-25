package br.com.conciliacao.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "produto")
public class Produto {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, unique = true, length = 40) private String sku;
  @Column(nullable = false, length = 150) private String nome;
  @Column(nullable = false, length = 10) private String unidade = "UN";
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal precoVenda = BigDecimal.ZERO;
  @Column(nullable = false, precision = 15, scale = 2) private BigDecimal precoCusto = BigDecimal.ZERO;
  @Column(nullable = false, precision = 15, scale = 3) private BigDecimal estoque = BigDecimal.ZERO;
  @Column(nullable = false, precision = 15, scale = 3) private BigDecimal estoqueMinimo = BigDecimal.ZERO;
  @Column(nullable = false) private boolean ativo = true;

  protected Produto() {}

  public Produto(String sku, String nome, String unidade, BigDecimal precoVenda, BigDecimal precoCusto,
      BigDecimal estoque, BigDecimal estoqueMinimo) {
    atualizar(sku, nome, unidade, precoVenda, precoCusto, estoque, estoqueMinimo);
  }

  public void atualizar(String sku, String nome, String unidade, BigDecimal precoVenda, BigDecimal precoCusto,
      BigDecimal estoque, BigDecimal estoqueMinimo) {
    this.sku = Texto.maiusculo(sku == null ? null : sku.trim());
    this.nome = Texto.maiusculo(nome);
    this.unidade = unidade == null || unidade.isBlank() ? "UN" : Texto.maiusculo(unidade.trim());
    this.precoVenda = dinheiro(precoVenda);
    this.precoCusto = dinheiro(precoCusto);
    this.estoque = quantidade(estoque);
    this.estoqueMinimo = quantidade(estoqueMinimo);
  }

  public void ajustarEstoque(BigDecimal delta) {
    this.estoque = quantidade(this.estoque.add(delta));
  }

  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }
  public boolean isEstoqueBaixo() { return estoque.compareTo(estoqueMinimo) <= 0; }

  public Long getId() { return id; }
  public String getSku() { return sku; }
  public String getNome() { return nome; }
  public String getUnidade() { return unidade; }
  public BigDecimal getPrecoVenda() { return precoVenda; }
  public BigDecimal getPrecoCusto() { return precoCusto; }
  public BigDecimal getEstoque() { return estoque; }
  public BigDecimal getEstoqueMinimo() { return estoqueMinimo; }
  public boolean isAtivo() { return ativo; }

  static BigDecimal dinheiro(BigDecimal valor) {
    return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
  }

  static BigDecimal quantidade(BigDecimal valor) {
    return (valor == null ? BigDecimal.ZERO : valor).setScale(3, RoundingMode.HALF_UP);
  }
}
