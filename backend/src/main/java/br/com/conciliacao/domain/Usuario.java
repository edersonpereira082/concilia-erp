package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @JsonIgnore @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "software_house_id") private SoftwareHouse softwareHouse;
  @Column(nullable = false, length = 120) private String nome;
  @Column(nullable = false, unique = true, length = 150) private String email;
  @JsonIgnore @Column(name = "senha_hash", nullable = false, length = 100) private String senhaHash;
  @Column(nullable = false, length = 20) private String perfil = "ADMIN";
  @Column(nullable = false) private boolean ativo = true;

  protected Usuario() {}
  public Usuario(SoftwareHouse softwareHouse, String nome, String email, String senhaHash) {
    this.softwareHouse = softwareHouse;
    this.nome = Texto.maiusculo(nome);
    this.email = email;
    this.senhaHash = senhaHash;
  }
  public Usuario(SoftwareHouse softwareHouse, String nome, String email, String senhaHash, String perfil) {
    this(softwareHouse, nome, email, senhaHash);
    this.perfil = Texto.maiusculo(perfil);
  }
  public Long getId() { return id; }
  public String getNome() { return nome; }
  public String getEmail() { return email; }
  public String getPerfil() { return perfil; }
  public boolean isAtivo() { return ativo; }
  public SoftwareHouse getSoftwareHouse() { return softwareHouse; }
  public String getSenhaHash() { return senhaHash; }
  public void atualizar(String nome, String email, String perfil) {
    this.nome = Texto.maiusculo(nome);
    this.email = email;
    if (perfil != null && !perfil.isBlank()) this.perfil = Texto.maiusculo(perfil);
  }
  public void alterarSenha(String senhaHash) { this.senhaHash = senhaHash; }
  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }
}