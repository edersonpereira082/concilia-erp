package br.com.conciliacao.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SistemaLogRepository extends JpaRepository<SistemaLog, Long> {
  List<SistemaLog> findBySoftwareHouse_IdOrderByIdDesc(Long softwareHouseId);
}
