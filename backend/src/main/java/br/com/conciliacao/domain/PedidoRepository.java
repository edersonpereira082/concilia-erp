package br.com.conciliacao.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
  long countByTipo(TipoPedido tipo);

  @Query("select distinct p from Pedido p join fetch p.empresa left join fetch p.formaPagamento fp left join fetch fp.caixa left join fetch fp.contaCorrente left join fetch p.transportadora left join fetch p.itens i left join fetch i.produto")
  List<Pedido> findAllDetalhado();

  @Query("select distinct p from Pedido p join fetch p.empresa left join fetch p.formaPagamento fp left join fetch fp.caixa left join fetch fp.contaCorrente left join fetch p.transportadora left join fetch p.itens i left join fetch i.produto where p.tipo = :tipo")
  List<Pedido> findDetalhadoByTipo(TipoPedido tipo);

  @Query("select distinct p from Pedido p join fetch p.empresa left join fetch p.formaPagamento fp left join fetch fp.caixa left join fetch fp.contaCorrente left join fetch p.transportadora left join fetch p.itens i left join fetch i.produto where p.id = :id")
  Optional<Pedido> findDetalhadoById(Long id);

  @Query(value = "select coalesce(max(cast(substring(numero from 3) as integer)), 0) from pedido where tipo = :tipo", nativeQuery = true)
  Integer ultimoNumero(@Param("tipo") String tipo);
}
