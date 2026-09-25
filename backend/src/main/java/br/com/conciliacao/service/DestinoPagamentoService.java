package br.com.conciliacao.service;

import br.com.conciliacao.domain.FormaPagamento;
import br.com.conciliacao.domain.TipoDestinoPagamento;
import br.com.conciliacao.domain.Titulo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class DestinoPagamentoService {
  private final CaixaService caixaService;
  private final ContaCorrenteService contaCorrenteService;

  public DestinoPagamentoService(CaixaService caixaService, ContaCorrenteService contaCorrenteService) {
    this.caixaService = caixaService;
    this.contaCorrenteService = contaCorrenteService;
  }

  public void garantir(FormaPagamento forma, boolean aVista) {
    if (forma == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione a forma de pagamento");
    if (forma.getTipoDestino() == TipoDestinoPagamento.CAIXA) {
      if (forma.getCaixa() == null || !forma.getCaixa().isAtivo())
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Forma de pagamento sem caixa de destino");
      if (aVista) caixaService.garantirAberto(forma.getCaixa());
      return;
    }
    if (forma.getContaCorrente() == null || !forma.getContaCorrente().isAtivo())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Forma de pagamento sem conta bancária de destino (corrente ou poupança)");
  }

  public void disponibilizar(Titulo titulo) {
    var pedido = titulo.getPedido();
    if (pedido == null || pedido.getFormaPagamento() == null) return;
    var forma = pedido.getFormaPagamento();
    titulo.definirDestino(forma.getContaCorrente(), forma.getCaixa());
    if (forma.getTipoDestino() == TipoDestinoPagamento.CAIXA) caixaService.disponibilizar(titulo);
    else contaCorrenteService.disponibilizar(titulo);
  }
}
