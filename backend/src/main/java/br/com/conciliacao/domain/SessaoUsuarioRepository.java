package br.com.conciliacao.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface SessaoUsuarioRepository extends JpaRepository<SessaoUsuario, String> {
  @Query("select s from SessaoUsuario s join fetch s.usuario u join fetch u.softwareHouse where s.token = :token and s.expiraEm > :agora")
  Optional<SessaoUsuario> findByTokenAndExpiraEmAfter(String token, java.time.LocalDateTime agora);
  @Modifying
  void deleteByUsuarioId(Long usuarioId);
}
