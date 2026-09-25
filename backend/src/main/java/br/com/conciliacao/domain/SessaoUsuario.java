package br.com.conciliacao.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessao_usuario")
public class SessaoUsuario {
  @Id @Column(length = 80) private String token;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id") private Usuario usuario;
  @Column(nullable = false) private LocalDateTime expiraEm;

  protected SessaoUsuario() {}
  public SessaoUsuario(String token, Usuario usuario, LocalDateTime expiraEm) {
    this.token = token; this.usuario = usuario; this.expiraEm = expiraEm;
  }
  public String getToken() { return token; }
  public Usuario getUsuario() { return usuario; }
  public LocalDateTime getExpiraEm() { return expiraEm; }
}