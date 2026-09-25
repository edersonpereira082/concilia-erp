package br.com.conciliacao.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "software_house")
public class SoftwareHouse {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotBlank @Size(max = 150) @Column(nullable = false, length = 150) private String razaoSocial;
  @Size(max = 150) @Column(length = 150) private String nomeFantasia;
  @NotBlank @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos")
  @Column(nullable = false, unique = true, length = 14) private String cnpj;
  @NotBlank @Email @Column(nullable = false, length = 150) private String email;
  @Size(max = 20) private String telefone;
  @Size(max = 120) private String cidade;
  @Size(max = 2) private String uf;

  protected SoftwareHouse() {}

  public SoftwareHouse(String razaoSocial, String nomeFantasia, String cnpj, String email,
      String telefone, String cidade, String uf) {
    this.razaoSocial = Texto.maiusculo(razaoSocial);
    this.nomeFantasia = Texto.maiusculo(nomeFantasia);
    this.cnpj = cnpj;
    this.email = email;
    this.telefone = telefone;
    this.cidade = Texto.maiusculo(cidade);
    this.uf = Texto.maiusculo(uf);
  }

  public Long getId() { return id; }
  public String getRazaoSocial() { return razaoSocial; }
  public String getNomeFantasia() { return nomeFantasia; }
  public String getCnpj() { return cnpj; }
  public String getEmail() { return email; }
  public String getTelefone() { return telefone; }
  public String getCidade() { return cidade; }
  public String getUf() { return uf; }
}