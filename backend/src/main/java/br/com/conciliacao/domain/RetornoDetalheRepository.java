package br.com.conciliacao.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import java.util.List;

public interface RetornoDetalheRepository extends JpaRepository<RetornoDetalhe, Long> {
  List<RetornoDetalhe> findByRetornoIdOrderByNumeroLinha(Long retornoId);
  @Modifying
  void deleteByRetornoId(Long retornoId);
}
