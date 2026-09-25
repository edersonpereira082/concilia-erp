package br.com.conciliacao.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
  Optional<Empresa> findByCnpj(String cnpj);
  Optional<Empresa> findByCpf(String cpf);
  List<Empresa> findByTipoIn(List<TipoEmpresa> tipos);
  boolean existsByCnpj(String cnpj);
  boolean existsByCpf(String cpf);
}
