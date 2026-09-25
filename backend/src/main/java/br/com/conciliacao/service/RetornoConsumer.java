package br.com.conciliacao.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RetornoConsumer {
  private final RetornoService service;
  public RetornoConsumer(RetornoService service) { this.service = service; }
  @RabbitListener(queues = "retorno-bancario")
  public void consumir(String retornoId) { service.processar(Long.valueOf(retornoId)); }
}
