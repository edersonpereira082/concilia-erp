package br.com.conciliacao.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaixaRepository extends JpaRepository<Caixa, Long> {
  boolean existsByDescricaoIgnoreCase(String descricao);
  Optional<Caixa> findByDescricaoIgnoreCase(String descricao);
}
