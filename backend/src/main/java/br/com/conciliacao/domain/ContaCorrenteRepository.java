package br.com.conciliacao.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaCorrenteRepository extends JpaRepository<ContaCorrente, Long> {
  boolean existsByDescricaoIgnoreCase(String descricao);
  Optional<ContaCorrente> findByDescricaoIgnoreCase(String descricao);
  Optional<ContaCorrente> findByBancoCodigoAndAgenciaAndConta(String bancoCodigo, String agencia, String conta);
}
