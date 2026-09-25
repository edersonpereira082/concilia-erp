package br.com.conciliacao.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransportadoraRepository extends JpaRepository<Transportadora, Long> {
  boolean existsByNomeIgnoreCase(String nome);
  Optional<Transportadora> findByNomeIgnoreCase(String nome);
}
