package br.com.conciliacao.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Long> {
  boolean existsByNomeIgnoreCase(String nome);
  Optional<FormaPagamento> findByNomeIgnoreCase(String nome);
  boolean existsByCaixa_Id(Long caixaId);
  boolean existsByContaCorrente_Id(Long contaCorrenteId);

  @Query("select f from FormaPagamento f left join fetch f.caixa left join fetch f.contaCorrente")
  List<FormaPagamento> findAllComDestino();

  @Query("select f from FormaPagamento f left join fetch f.caixa left join fetch f.contaCorrente where f.id = :id")
  Optional<FormaPagamento> findComDestinoById(Long id);
}
