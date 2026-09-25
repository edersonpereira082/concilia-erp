package br.com.conciliacao.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CaixaMovimentoRepository extends JpaRepository<CaixaMovimento, Long> {
  boolean existsByTitulo_Id(Long tituloId);
}
