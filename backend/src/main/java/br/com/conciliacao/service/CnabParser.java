package br.com.conciliacao.service;

import br.com.conciliacao.domain.ResultadoConciliacao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CnabParser {
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("ddMMyyyy");

  public List<DetalheCnab> parse(List<String> linhas) {
    if (linhas == null || linhas.isEmpty()) throw new IllegalArgumentException("Arquivo CNAB sem linhas");
    var layout = detectarLayout(linhas);
    var detalhes = new ArrayList<DetalheCnab>();
    for (int index = 0; index < linhas.size(); index++) {
      var linha = linhas.get(index);
      if (ehDetalhe(linha, layout)) detalhes.add(parseDetalhe(linha, index + 1, layout));
    }
    if (detalhes.isEmpty()) throw new IllegalArgumentException("Arquivo CNAB sem registros de detalhe");
    return detalhes;
  }

  private String detectarLayout(List<String> linhas) {
    var maior = linhas.stream().mapToInt(String::length).max().orElse(0);
    if (maior >= 400) return "CNAB400";
    if (maior >= 240) return "CNAB240";
    throw new IllegalArgumentException("Layout CNAB deve ter registros de 240 ou 400 posicoes");
  }

  private boolean ehDetalhe(String linha, String layout) {
    if ("CNAB400".equals(layout)) return linha.length() >= 400 && charAt(linha, 0) == '1';
    return linha.length() >= 240 && charAt(linha, 7) == '3' && "TU".indexOf(charAt(linha, 13)) >= 0;
  }

  private DetalheCnab parseDetalhe(String linha, int numeroLinha, String layout) {
    var nossoNumero = "CNAB400".equals(layout) ? campo(linha, 62, 73) : campo(linha, 37, 62);
    var codigo = "CNAB400".equals(layout) ? campo(linha, 108, 110) : campo(linha, 15, 18);
    var valor = dinheiro("CNAB400".equals(layout) ? campo(linha, 152, 165) : campo(linha, 81, 96));
    var data = data("CNAB400".equals(layout) ? campo(linha, 110, 116) : campo(linha, 70, 78));
    var regra = classificar(codigo, nossoNumero, valor);
    return new DetalheCnab(numeroLinha, layout, linha, vazio(nossoNumero), vazio(codigo), valor, data, regra.resultado(), regra.motivo());
  }

  private Regra classificar(String codigo, String nossoNumero, BigDecimal valor) {
    if (nossoNumero.isBlank()) return new Regra(ResultadoConciliacao.NAO_IDENTIFICADO, "Nosso numero ausente");
    if (codigo.isBlank()) return new Regra(ResultadoConciliacao.PENDENTE, "Codigo de ocorrencia ausente");
    return switch (codigo.trim()) {
      case "06", "06 ", "LIQ" -> new Regra(ResultadoConciliacao.LIQUIDADO, "Pagamento liquidado");
      case "09", "09 ", "BX", "BAI" -> new Regra(ResultadoConciliacao.BAIXADO, "Titulo baixado");
      case "03", "03 ", "REJ" -> new Regra(ResultadoConciliacao.REJEITADO, "Titulo rejeitado");
      default -> valor == null ? new Regra(ResultadoConciliacao.PENDENTE, "Ocorrencia pendente") : new Regra(ResultadoConciliacao.PENDENTE, "Ocorrencia nao liquidada");
    };
  }

  private String campo(String linha, int inicio, int fim) { return linha.substring(inicio, Math.min(fim, linha.length())).trim(); }
  private char charAt(String linha, int indice) { return linha.charAt(indice); }
  private String vazio(String valor) { return valor == null ? "" : valor; }
  private BigDecimal dinheiro(String valor) {
    if (valor.isBlank() || valor.chars().allMatch(c -> c == '0')) return null;
    try { return new BigDecimal(valor.replaceAll("\\D", "")).movePointLeft(2); }
    catch (NumberFormatException error) { return null; }
  }
  private LocalDate data(String valor) {
    if (valor.length() != 8 || valor.chars().allMatch(c -> c == '0')) return null;
    try { return LocalDate.parse(valor, DATE); }
    catch (DateTimeParseException error) { return null; }
  }

  public record DetalheCnab(int numeroLinha, String layout, String linha, String nossoNumero,
      String codigoOcorrencia, BigDecimal valor, LocalDate dataOcorrencia,
      ResultadoConciliacao resultado, String motivo) {}
  private record Regra(ResultadoConciliacao resultado, String motivo) {}
}
