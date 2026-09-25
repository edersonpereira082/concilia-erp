package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "forma_pagamento")
public class FormaPagamento {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, unique = true, length = 80) private String nome;
  @Column(nullable = false) private int parcelas = 1;
  @Column(nullable = false) private int diasVencimento = 0;
  @Enumerated(EnumType.STRING) @Column(name = "tipo_destino", nullable = false, length = 20)
  private TipoDestinoPagamento tipoDestino = TipoDestinoPagamento.CAIXA;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "conta_corrente_id")
  @JsonIgnoreProperties({"bancoCodigo", "bancoNome", "agencia", "agenciaDigito", "conta", "contaDigito", "titular", "pix", "movimentos"})
  private ContaCorrente contaCorrente;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "caixa_id")
  @JsonIgnoreProperties({"sessaoAberta", "saldoAtual", "statusOperacao"})
  private Caixa caixa;
  @Column(nullable = false) private boolean ativo = true;

  protected FormaPagamento() {}

  public FormaPagamento(String nome, int parcelas, int diasVencimento, TipoDestinoPagamento tipoDestino,
      ContaCorrente contaCorrente, Caixa caixa) {
    atualizar(nome, parcelas, diasVencimento, tipoDestino, contaCorrente, caixa);
  }

  public void atualizar(String nome, int parcelas, int diasVencimento, TipoDestinoPagamento tipoDestino,
      ContaCorrente contaCorrente, Caixa caixa) {
    this.nome = Texto.maiusculo(nome);
    this.parcelas = Math.max(1, parcelas);
    this.diasVencimento = Math.max(0, diasVencimento);
    this.tipoDestino = tipoDestino == null ? TipoDestinoPagamento.CAIXA : tipoDestino;
    if (this.tipoDestino == TipoDestinoPagamento.CONTA_CORRENTE) {
      if (contaCorrente == null) throw new IllegalArgumentException("Selecione a conta corrente de destino");
      this.contaCorrente = contaCorrente;
      this.caixa = null;
    } else {
      if (caixa == null) throw new IllegalArgumentException("Selecione o caixa de destino");
      this.caixa = caixa;
      this.contaCorrente = null;
    }
  }

  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }

  public Long getId() { return id; }
  public String getNome() { return nome; }
  public int getParcelas() { return parcelas; }
  public int getDiasVencimento() { return diasVencimento; }
  public TipoDestinoPagamento getTipoDestino() { return tipoDestino; }
  public ContaCorrente getContaCorrente() { return contaCorrente; }
  public Caixa getCaixa() { return caixa; }
  public boolean isAtivo() { return ativo; }

  public String getDestinoDescricao() {
    if (tipoDestino == TipoDestinoPagamento.CONTA_CORRENTE && contaCorrente != null) {
      var tipo = contaCorrente.getTipo() == TipoConta.POUPANCA ? "POUPANÇA" : "CORRENTE";
      return contaCorrente.getDescricao() + " · " + tipo;
    }
    if (caixa != null) return caixa.getDescricao();
    return tipoDestino == TipoDestinoPagamento.CONTA_CORRENTE ? "BANCO" : "CAIXA";
  }
}
