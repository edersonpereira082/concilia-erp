package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "caixa")
public class Caixa {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, unique = true, length = 80) private String descricao;
  @Column(nullable = false) private boolean ativo = true;
  @Transient @JsonIgnoreProperties({"caixa"})
  private CaixaSessao sessaoAberta;

  protected Caixa() {}

  public Caixa(String descricao) {
    atualizar(descricao);
  }

  public void atualizar(String descricao) {
    this.descricao = Texto.maiusculo(descricao == null ? null : descricao.trim());
    if (this.descricao == null || this.descricao.isBlank())
      throw new IllegalArgumentException("Descrição é obrigatória");
  }

  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }

  public void definirSessaoAberta(CaixaSessao sessao) { this.sessaoAberta = sessao; }

  public Long getId() { return id; }
  public String getDescricao() { return descricao; }
  public boolean isAtivo() { return ativo; }
  public CaixaSessao getSessaoAberta() { return sessaoAberta; }
  public String getStatusOperacao() { return sessaoAberta == null ? "FECHADO" : "ABERTO"; }
  public BigDecimal getSaldoAtual() { return sessaoAberta == null ? null : sessaoAberta.getSaldoCalculado(); }
}
