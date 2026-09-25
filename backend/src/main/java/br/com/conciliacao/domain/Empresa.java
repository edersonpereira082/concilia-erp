package br.com.conciliacao.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "empresa", uniqueConstraints = {
    @UniqueConstraint(name = "uk_empresa_cnpj", columnNames = "cnpj"),
    @UniqueConstraint(name = "uk_empresa_cpf", columnNames = "cpf")
})
public class Empresa {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_pessoa", nullable = false, length = 20)
  private TipoPessoa tipoPessoa = TipoPessoa.JURIDICA;

  @NotBlank @Size(max = 150)
  @Column(nullable = false, length = 150)
  private String razaoSocial;

  @Size(max = 150)
  @Column(length = 150)
  private String nomeFantasia;

  @Column(length = 11)
  private String cpf;

  @Column(length = 14)
  private String cnpj;

  @Size(max = 20)
  @Column(length = 20)
  private String rg;

  @Size(max = 11)
  @Column(name = "inscricao_estadual", length = 20)
  private String inscricaoEstadual;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TipoEmpresa tipo;

  @Email @Size(max = 150)
  @Column(length = 150)
  private String email;

  @Size(max = 20)
  @Column(length = 20)
  private String telefone;

  @Column(length = 11)
  private String celular;

  @Column(length = 8)
  private String cep;

  @Size(max = 150)
  @Column(length = 150)
  private String logradouro;

  @Size(max = 20)
  @Column(length = 20)
  private String numero;

  @Size(max = 80)
  @Column(length = 80)
  private String complemento;

  @Size(max = 80)
  @Column(length = 80)
  private String bairro;

  @Size(max = 120)
  @Column(length = 120)
  private String cidade;

  @Size(max = 2)
  @Column(length = 2)
  private String uf;

  @Column(nullable = false)
  private boolean ativo = true;

  protected Empresa() {}

  public Empresa(String razaoSocial, String nomeFantasia, String cnpj, TipoEmpresa tipo,
      String email, String telefone, String cidade, String uf) {
    atualizar(new Dados(TipoPessoa.JURIDICA, razaoSocial, nomeFantasia, null, cnpj, null, null, tipo,
        email, telefone, null, null, null, null, null, null, cidade, uf));
  }

  public Empresa(Dados dados) {
    atualizar(dados);
  }

  public void atualizar(Dados dados) {
    this.tipoPessoa = dados.tipoPessoa() == null ? TipoPessoa.JURIDICA : dados.tipoPessoa();
    this.razaoSocial = Texto.maiusculo(obrigatorio(dados.razaoSocial(), this.tipoPessoa == TipoPessoa.FISICA ? "Nome" : "Razão social"));
    this.tipo = dados.tipo();
    this.email = dados.email();
    this.telefone = contato(dados.telefone(), 10, "Telefone");
    this.celular = contato(dados.celular(), 11, "Celular");
    this.cep = soDigitos(dados.cep(), 8);
    this.logradouro = Texto.maiusculo(vazioParaNulo(dados.logradouro()));
    this.numero = Texto.maiusculo(vazioParaNulo(dados.numero()));
    this.complemento = Texto.maiusculo(vazioParaNulo(dados.complemento()));
    this.bairro = Texto.maiusculo(vazioParaNulo(dados.bairro()));
    this.cidade = Texto.maiusculo(vazioParaNulo(dados.cidade()));
    this.uf = Texto.maiusculo(vazioParaNulo(dados.uf()));
    if (this.tipoPessoa == TipoPessoa.FISICA) {
      this.cpf = documento(dados.cpf(), 11, "CPF");
      this.cnpj = null;
      this.rg = Texto.maiusculo(vazioParaNulo(dados.rg()));
      this.nomeFantasia = null;
      this.inscricaoEstadual = null;
    } else {
      this.cnpj = documento(dados.cnpj(), 14, "CNPJ");
      this.cpf = null;
      this.rg = null;
      this.nomeFantasia = Texto.maiusculo(vazioParaNulo(dados.nomeFantasia()));
      var ie = Texto.maiusculo(vazioParaNulo(dados.inscricaoEstadual()));
      this.inscricaoEstadual = ie == null || ie.length() <= 11 ? ie : ie.substring(0, 11);
    }
  }

  public void alterarAtivo(boolean ativo) { this.ativo = ativo; }

  @Transient
  public String getDocumento() {
    return tipoPessoa == TipoPessoa.FISICA ? cpf : cnpj;
  }

  public Long getId() { return id; }
  public TipoPessoa getTipoPessoa() { return tipoPessoa; }
  public String getRazaoSocial() { return razaoSocial; }
  public String getNomeFantasia() { return nomeFantasia; }
  public String getCpf() { return cpf; }
  public String getCnpj() { return cnpj; }
  public String getRg() { return rg; }
  public String getInscricaoEstadual() { return inscricaoEstadual; }
  public TipoEmpresa getTipo() { return tipo; }
  public String getEmail() { return email; }
  public String getTelefone() { return telefone; }
  public String getCelular() { return celular; }
  public String getCep() { return cep; }
  public String getLogradouro() { return logradouro; }
  public String getNumero() { return numero; }
  public String getComplemento() { return complemento; }
  public String getBairro() { return bairro; }
  public String getCidade() { return cidade; }
  public String getUf() { return uf; }
  public boolean isAtivo() { return ativo; }

  private static String documento(String valor, int tamanho, String campo) {
    var limpo = soDigitos(valor, tamanho);
    if (limpo == null || limpo.length() != tamanho)
      throw new IllegalArgumentException(campo + " deve conter " + tamanho + " dígitos");
    return limpo;
  }

  private static String contato(String valor, int tamanho, String campo) {
    var limpo = soDigitos(valor, tamanho);
    if (limpo == null) return null;
    if (limpo.length() != tamanho)
      throw new IllegalArgumentException(campo + " deve conter " + tamanho + " dígitos");
    return limpo;
  }

  private static String soDigitos(String valor, int maximo) {
    if (valor == null) return null;
    var limpo = valor.replaceAll("\\D", "");
    if (limpo.isBlank()) return null;
    return limpo.length() > maximo ? limpo.substring(0, maximo) : limpo;
  }

  private static String vazioParaNulo(String valor) {
    return valor == null || valor.isBlank() ? null : valor.trim();
  }

  private static String obrigatorio(String valor, String campo) {
    if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " é obrigatório");
    return valor.trim();
  }

  public record Dados(TipoPessoa tipoPessoa, String razaoSocial, String nomeFantasia, String cpf, String cnpj,
      String rg, String inscricaoEstadual, TipoEmpresa tipo, String email, String telefone, String celular, String cep,
      String logradouro, String numero, String complemento, String bairro, String cidade, String uf) {}
}
