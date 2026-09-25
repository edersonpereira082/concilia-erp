package br.com.conciliacao.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SoftwareHouseRepository extends JpaRepository<SoftwareHouse, Long> {
  boolean existsByCnpj(String cnpj);
  Optional<SoftwareHouse> findByCnpj(String cnpj);
}
