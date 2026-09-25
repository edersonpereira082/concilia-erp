package br.com.conciliacao.service;

import br.com.conciliacao.domain.ResultadoConciliacao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CnabParserTest {
  private final CnabParser parser = new CnabParser();

  @Test void deveClassificarLiquidacaoNoCnab400() {
    var linha = linha400("12345678901", "06", "0000000012345", "10092026");

    var detalhe = parser.parse(List.of("0".repeat(400), linha)).getFirst();

    assertThat(detalhe.layout()).isEqualTo("CNAB400");
    assertThat(detalhe.nossoNumero()).isEqualTo("12345678901");
    assertThat(detalhe.codigoOcorrencia()).isEqualTo("06");
    assertThat(detalhe.valor()).isEqualByComparingTo("123.45");
    assertThat(detalhe.resultado()).isEqualTo(ResultadoConciliacao.LIQUIDADO);
  }

  @Test void deveRejeitarArquivoForaDoLayout() {
    assertThatThrownBy(() -> parser.parse(List.of("header", "detalhe")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("240 ou 400");
  }

  private String linha400(String nossoNumero, String ocorrencia, String valor, String data) {
    var linha = new StringBuilder("1".repeat(400));
    put(linha, 62, nossoNumero);
    put(linha, 108, ocorrencia);
    put(linha, 110, data);
    put(linha, 152, valor);
    return linha.toString();
  }

  private void put(StringBuilder linha, int inicio, String valor) {
    linha.replace(inicio, inicio + valor.length(), valor);
  }
}