package br.com.conciliacao.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "retorno_bancario")
public class RetornoBancario {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false) private String nomeArquivo;
  @Column(nullable = false) private LocalDate dataProcessamento;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private StatusRetorno status;
  private int registros;
  @Column(nullable = false) private boolean ativo = true;

  protected RetornoBancario() {}
  public RetornoBancario(String nomeArquivo, LocalDate dataProcessamento, int registros) {
    this.nomeArquivo = Texto.maiusculo(nomeArquivo); this.dataProcessamento = dataProcessamento;
    this.registros = registros; this.status = StatusRetorno.RECEBIDO;
  }
  public Long getId() { return id; }
  public String getNomeArquivo() { return nomeArquivo; }
  public LocalDate getDataProcessamento() { return dataProcessamento; }
  public StatusRetorno getStatus() { return status; }
  public int getRegistros() { return registros; }
  public boolean isAtivo() { return ativo; }
  public void processar() { this.status = StatusRetorno.PROCESSADO; }
  public void atualizarNome(String nomeArquivo) { this.nomeArquivo = Texto.maiusculo(nomeArquivo); }
  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }
}
