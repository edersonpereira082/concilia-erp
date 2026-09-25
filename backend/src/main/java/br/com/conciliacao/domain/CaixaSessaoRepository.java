package br.com.conciliacao.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CaixaSessaoRepository extends JpaRepository<CaixaSessao, Long> {
  Optional<CaixaSessao> findByCaixa_IdAndStatus(Long caixaId, StatusCaixaSessao status);

  @Query("select distinct s from CaixaSessao s join fetch s.caixa left join fetch s.movimentos left join fetch s.usuarioAbertura "
      + "left join fetch s.usuarioFechamento where s.status = :status")
  List<CaixaSessao> findDetalhadoByStatus(StatusCaixaSessao status);

  @Query("select distinct s from CaixaSessao s join fetch s.caixa left join fetch s.movimentos left join fetch s.usuarioAbertura "
      + "left join fetch s.usuarioFechamento where s.caixa.id = :caixaId and s.status = :status")
  Optional<CaixaSessao> findDetalhadoByCaixaIdAndStatus(Long caixaId, StatusCaixaSessao status);

  boolean existsByCaixa_Id(Long caixaId);
}
