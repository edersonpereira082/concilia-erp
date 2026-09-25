package br.com.conciliacao.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TituloRepository extends JpaRepository<Titulo, Long> {
  @Query("select t from Titulo t join fetch t.empresa left join fetch t.contaCorrente left join fetch t.caixa order by t.vencimento asc")
  List<Titulo> findAllByOrderByVencimentoAsc();
  @Query("select t from Titulo t join fetch t.empresa left join fetch t.contaCorrente left join fetch t.caixa where t.tipo = :tipo order by t.vencimento asc")
  List<Titulo> findByTipoOrderByVencimentoAsc(TipoTitulo tipo);
  List<Titulo> findByPedido_Id(Long pedidoId);

  @Query("select t from Titulo t join fetch t.empresa left join fetch t.pedido p left join fetch p.formaPagamento fp left join fetch fp.caixa left join fetch fp.contaCorrente where t.id = :id")
  Optional<Titulo> findDetalhadoById(Long id);
}
