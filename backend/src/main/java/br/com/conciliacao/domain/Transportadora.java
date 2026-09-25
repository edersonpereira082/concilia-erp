package br.com.conciliacao.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "transportadora")
public class Transportadora {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, unique = true, length = 150) private String nome;
  @Column(length = 14) private String cnpj;
  @Column(length = 20) private String telefone;
  @Column(nullable = false) private boolean ativo = true;

  protected Transportadora() {}

  public Transportadora(String nome, String cnpj, String telefone) {
    atualizar(nome, cnpj, telefone);
  }

  public void atualizar(String nome, String cnpj, String telefone) {
    this.nome = Texto.maiusculo(nome);
    this.cnpj = cnpj == null || cnpj.isBlank() ? null : cnpj.replaceAll("\\D", "");
    this.telefone = telefone;
  }

  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }

  public Long getId() { return id; }
  public String getNome() { return nome; }
  public String getCnpj() { return cnpj; }
  public String getTelefone() { return telefone; }
  public boolean isAtivo() { return ativo; }
}
