package br.com.conciliacao.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sistema_log")
public class SistemaLog {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @JsonIgnore @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "software_house_id")
  private SoftwareHouse softwareHouse;
  @Column(name = "data_hora", nullable = false) private LocalDateTime dataHora;
  @Column(nullable = false, length = 40) private String acao;
  @Column(nullable = false, length = 10) private String metodo;
  @Column(nullable = false, length = 180) private String recurso;
  @Column(nullable = false, length = 240) private String descricao;
  @Column(name = "usuario_nome", nullable = false, length = 120) private String usuarioNome;
  @Column(name = "usuario_email", nullable = false, length = 150) private String usuarioEmail;
  @Column(name = "confirmador_nome", length = 120) private String confirmadorNome;
  @Column(name = "confirmador_email", length = 150) private String confirmadorEmail;
  @Column(name = "status_http", nullable = false) private int statusHttp;

  protected SistemaLog() {}

  public SistemaLog(SoftwareHouse softwareHouse, String acao, String metodo, String recurso, String descricao,
      String usuarioNome, String usuarioEmail, String confirmadorNome, String confirmadorEmail, int statusHttp) {
    this.softwareHouse = softwareHouse;
    this.dataHora = LocalDateTime.now();
    this.acao = Texto.maiusculo(acao);
    this.metodo = metodo;
    this.recurso = recurso;
    this.descricao = descricao;
    this.usuarioNome = Texto.maiusculo(usuarioNome);
    this.usuarioEmail = usuarioEmail;
    this.confirmadorNome = confirmadorNome == null ? null : Texto.maiusculo(confirmadorNome);
    this.confirmadorEmail = confirmadorEmail;
    this.statusHttp = statusHttp;
  }

  public Long getId() { return id; }
  public LocalDateTime getDataHora() { return dataHora; }
  public String getAcao() { return acao; }
  public String getMetodo() { return metodo; }
  public String getRecurso() { return recurso; }
  public String getDescricao() { return descricao; }
  public String getUsuarioNome() { return usuarioNome; }
  public String getUsuarioEmail() { return usuarioEmail; }
  public String getConfirmadorNome() { return confirmadorNome; }
  public String getConfirmadorEmail() { return confirmadorEmail; }
  public int getStatusHttp() { return statusHttp; }
}
