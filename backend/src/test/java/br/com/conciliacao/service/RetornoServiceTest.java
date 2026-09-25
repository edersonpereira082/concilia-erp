package br.com.conciliacao.service;

import br.com.conciliacao.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;

class RetornoServiceTest {
  @Test void devePublicarRetornoNaFila() throws Exception {
    var retorno = mock(RetornoBancario.class);
    var rabbit = mock(RabbitTemplate.class);
    var detalhes = mock(RetornoDetalheRepository.class);
    var parser = mock(CnabParser.class);
    when(retorno.getId()).thenReturn(1L);
    when(retorno.getNomeArquivo()).thenReturn("retorno.ret");
    when(parser.parse(anyList())).thenReturn(List.of(new CnabParser.DetalheCnab(2, "CNAB400", "linha", "123", "06", null, null, ResultadoConciliacao.LIQUIDADO, "Pagamento liquidado")));
    var repository = mock(RetornoRepository.class, invocation -> retorno);
    var result = new RetornoService(repository, detalhes, rabbit, parser).receber(new MockMultipartFile("arquivo", "retorno.ret", "text/plain", "header\ndetalhe".getBytes()));
    assertThat(result.getNomeArquivo()).isEqualTo("retorno.ret"); verify(rabbit).convertAndSend(eq("retorno-bancario"), anyString());
  }

  @Test void deveRejeitarArquivoVazio() {
    var rabbit = mock(RabbitTemplate.class);
    var repository = mock(RetornoRepository.class);
    var detalhes = mock(RetornoDetalheRepository.class);
    var parser = mock(CnabParser.class);
    var arquivo = new MockMultipartFile("arquivo", "retorno.ret", "text/plain", new byte[0]);

    assertThatThrownBy(() -> new RetornoService(repository, detalhes, rabbit, parser).receber(arquivo))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Arquivo de retorno vazio");
    verifyNoInteractions(repository, rabbit);
  }
}
