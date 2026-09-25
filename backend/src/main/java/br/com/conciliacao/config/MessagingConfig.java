package br.com.conciliacao.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class MessagingConfig {
  @Bean Queue retornoQueue() { return new Queue("retorno-bancario", true); }
  @Bean(name = "taskExecutor") SimpleAsyncTaskExecutor taskExecutor() {
    var executor = new SimpleAsyncTaskExecutor("retorno-"); executor.setVirtualThreads(true); return executor;
  }
  @Bean SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
    var factory = new SimpleRabbitListenerContainerFactory(); factory.setConnectionFactory(connectionFactory);
    factory.setTaskExecutor(taskExecutor()); return factory;
  }
}
