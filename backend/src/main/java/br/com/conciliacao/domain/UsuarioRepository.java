package br.com.conciliacao.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  @Query("select u from Usuario u join fetch u.softwareHouse where lower(u.email) = lower(:email)")
  Optional<Usuario> findByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCase(String email);
  @Query("select u from Usuario u join fetch u.softwareHouse where u.softwareHouse.id = :softwareHouseId order by u.nome")
  java.util.List<Usuario> findBySoftwareHouseIdOrderByNome(Long softwareHouseId);
  @Query("select u from Usuario u join fetch u.softwareHouse where u.id = :id")
  Optional<Usuario> findComSoftwareHouseById(Long id);
}
