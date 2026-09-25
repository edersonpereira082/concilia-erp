package br.com.conciliacao.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContaMovimentoRepository extends JpaRepository<ContaMovimento, Long> {
  boolean existsByTitulo_Id(Long tituloId);
  boolean existsByContaCorrente_Id(Long contaCorrenteId);
  List<ContaMovimento> findByContaCorrente_IdOrderByIdDesc(Long contaCorrenteId);

  @Query("select m from ContaMovimento m join fetch m.contaCorrente order by m.id desc")
  List<ContaMovimento> findAllDetalhado();
}
