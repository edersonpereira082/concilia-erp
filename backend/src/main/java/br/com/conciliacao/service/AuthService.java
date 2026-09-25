package br.com.conciliacao.service;

import br.com.conciliacao.domain.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
  private final SoftwareHouseRepository softwareHouseRepository;
  private final UsuarioRepository usuarioRepository;
  private final SessaoUsuarioRepository sessaoRepository;
  private final SistemaLogService logService;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthService(SoftwareHouseRepository softwareHouseRepository, UsuarioRepository usuarioRepository,
      SessaoUsuarioRepository sessaoRepository, SistemaLogService logService) {
    this.softwareHouseRepository = softwareHouseRepository;
    this.usuarioRepository = usuarioRepository;
    this.sessaoRepository = sessaoRepository;
    this.logService = logService;
  }

  public ResultadoAuth cadastrar(Cadastro cadastro) {
    validarSenha(cadastro.senha());
    if (usuarioRepository.existsByEmailIgnoreCase(cadastro.email()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail de usuário já cadastrado");
    if (softwareHouseRepository.existsByCnpj(cadastro.cnpj()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ da software house já cadastrado");
    try {
      var house = softwareHouseRepository.save(new SoftwareHouse(cadastro.razaoSocial(), cadastro.nomeFantasia(),
          cadastro.cnpj(), cadastro.emailEmpresa(), cadastro.telefone(), cadastro.cidade(), cadastro.uf()));
      var usuario = usuarioRepository.save(new Usuario(house, cadastro.nome(), cadastro.email().toLowerCase(),
          passwordEncoder.encode(cadastro.senha())));
      return criarSessao(usuario);
    } catch (DataIntegrityViolationException error) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Dados de cadastro já utilizados", error);
    }
  }

  public ResultadoAuth entrar(String email, String senha) {
    var usuario = usuarioRepository.findByEmailIgnoreCase(email)
        .filter(Usuario::isAtivo)
        .filter(candidato -> passwordEncoder.matches(senha, candidato.getSenhaHash()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));
    var resultado = criarSessao(usuario);
    logService.registrar(usuario, usuario, "POST", "/api/auth/login", 200);
    return resultado;
  }

  public Usuario confirmarUsuario(Usuario solicitante, String email, String senha) {
    if (email == null || email.isBlank() || senha == null || senha.isBlank())
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Informe usuário e senha de um cadastro para confirmar");
    var usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
        .filter(Usuario::isAtivo)
        .filter(candidato -> passwordEncoder.matches(senha, candidato.getSenhaHash()))
        .filter(candidato -> solicitante.getSoftwareHouse() != null
            && candidato.getSoftwareHouse().getId().equals(solicitante.getSoftwareHouse().getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos"));
    return usuario;
  }

  public Usuario autenticar(String token) {
    if (token == null || token.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login necessário");
    return sessaoRepository.findByTokenAndExpiraEmAfter(token, LocalDateTime.now())
        .map(SessaoUsuario::getUsuario)
        .filter(Usuario::isAtivo)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada"));
  }

  public void sair(String token) {
    if (token == null) return;
    sessaoRepository.findByTokenAndExpiraEmAfter(token, LocalDateTime.now()).ifPresent(sessao -> {
      logService.registrar(sessao.getUsuario(), sessao.getUsuario(), "POST", "/api/auth/logout", 204);
    });
    sessaoRepository.deleteById(token);
  }

  public Usuario cadastrarUsuario(Usuario solicitante, CadastroUsuario cadastro) {
    if (!"ADMIN".equals(solicitante.getPerfil()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem cadastrar usuários");
    validarSenha(cadastro.senha());
    if (usuarioRepository.existsByEmailIgnoreCase(cadastro.email()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail de usuário já cadastrado");
    return usuarioRepository.save(new Usuario(solicitante.getSoftwareHouse(), cadastro.nome(),
        cadastro.email().toLowerCase(), passwordEncoder.encode(cadastro.senha()),
        cadastro.perfil() == null || cadastro.perfil().isBlank() ? "OPERADOR" : cadastro.perfil()));
  }

  public java.util.List<Usuario> listarUsuarios(Usuario solicitante) {
    return usuarioRepository.findBySoftwareHouseIdOrderByNome(solicitante.getSoftwareHouse().getId());
  }

  public Usuario atualizarUsuario(Usuario solicitante, Long id, CadastroUsuario cadastro) {
    var usuario = usuarioDaCasa(solicitante, id);
    if (usuarioRepository.findByEmailIgnoreCase(cadastro.email())
        .filter(encontrado -> !encontrado.getId().equals(id)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail de usuário já cadastrado");
    usuario.atualizar(cadastro.nome(), cadastro.email().toLowerCase(), cadastro.perfil());
    if (cadastro.senha() != null && !cadastro.senha().isBlank()) {
      validarSenha(cadastro.senha());
      usuario.alterarSenha(passwordEncoder.encode(cadastro.senha()));
    }
    return usuarioRepository.save(usuario);
  }

  public Usuario alterarAtivo(Usuario solicitante, Long id, boolean ativo) {
    var usuario = usuarioDaCasa(solicitante, id);
    if (!ativo) impedirAutoAlteracao(solicitante, usuario, "desativar");
    usuario.alterarAtivo(ativo);
    if (!ativo) sessaoRepository.deleteByUsuarioId(usuario.getId());
    return usuarioRepository.save(usuario);
  }

  public void excluirUsuario(Usuario solicitante, Long id) {
    var usuario = usuarioDaCasa(solicitante, id);
    impedirAutoAlteracao(solicitante, usuario, "excluir");
    sessaoRepository.deleteByUsuarioId(usuario.getId());
    usuarioRepository.delete(usuario);
  }

  private Usuario usuarioDaCasa(Usuario solicitante, Long id) {
    if (!"ADMIN".equals(solicitante.getPerfil()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores podem gerenciar usuários");
    var usuario = usuarioRepository.findComSoftwareHouseById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    if (!usuario.getSoftwareHouse().getId().equals(solicitante.getSoftwareHouse().getId()))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
    return usuario;
  }

  private void impedirAutoAlteracao(Usuario solicitante, Usuario alvo, String acao) {
    if (solicitante.getId().equals(alvo.getId()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível " + acao + " o próprio usuário");
  }

  private ResultadoAuth criarSessao(Usuario usuario) {
    var token = UUID.randomUUID().toString();
    sessaoRepository.save(new SessaoUsuario(token, usuario, LocalDateTime.now().plusHours(12)));
    return new ResultadoAuth(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(),
        usuario.getSoftwareHouse().getNomeFantasia());
  }

  private void validarSenha(String senha) {
    if (senha == null || senha.length() < 8)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter ao menos 8 caracteres");
  }

  public record Cadastro(String nome, String email, String senha, String razaoSocial, String nomeFantasia,
      String cnpj, String emailEmpresa, String telefone, String cidade, String uf) {}
  public record CadastroUsuario(String nome, String email, String senha, String perfil) {}
  public record ResultadoAuth(String token, Long usuarioId, String nome, String email, String perfil, String softwareHouse) {}
}
