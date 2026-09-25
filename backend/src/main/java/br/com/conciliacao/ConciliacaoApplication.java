package br.com.conciliacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConciliacaoApplication {
  public static void main(String[] args) {
    SpringApplication.run(ConciliacaoApplication.class, args);
  }
}
