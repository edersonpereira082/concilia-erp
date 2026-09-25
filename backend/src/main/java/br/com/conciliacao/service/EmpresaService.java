package br.com.conciliacao.service;

import br.com.conciliacao.domain.Empresa;
import br.com.conciliacao.domain.EmpresaRepository;
import br.com.conciliacao.domain.TipoEmpresa;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EmpresaService {
  private final EmpresaRepository repository;

  public EmpresaService(EmpresaRepository repository) {
    this.repository = repository;
  }

  public Empresa criar(Empresa.Dados dados) {
    var empresa = from(dados);
    validarUnicidade(empresa, null);
    return repository.save(empresa);
  }

  public List<Empresa> listar(TipoEmpresa tipo) {
    if (tipo == null) return repository.findAll();
    return tipo == TipoEmpresa.AMBOS
        ? repository.findByTipoIn(List.of(TipoEmpresa.AMBOS))
        : repository.findByTipoIn(List.of(tipo, TipoEmpresa.AMBOS));
  }

  public Empresa buscar(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
  }

  public Empresa atualizar(Long id, Empresa.Dados dados) {
    Empresa empresa = buscar(id);
    var atualizada = from(dados);
    validarUnicidade(atualizada, id);
    try {
      empresa.atualizar(dados);
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
    return repository.save(empresa);
  }

  public Empresa alterarAtivo(Long id, boolean ativo) {
    Empresa empresa = buscar(id);
    empresa.alterarAtivo(ativo);
    return repository.save(empresa);
  }

  public void excluir(Long id) {
    Empresa empresa = buscar(id);
    try {
      repository.delete(empresa);
    } catch (DataIntegrityViolationException exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Empresa possui registros vinculados", exception);
    }
  }

  private void validarUnicidade(Empresa empresa, Long idAtual) {
    if (empresa.getCnpj() != null && repository.findByCnpj(empresa.getCnpj())
        .filter(encontrada -> !encontrada.getId().equals(idAtual)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado");
    if (empresa.getCpf() != null && repository.findByCpf(empresa.getCpf())
        .filter(encontrada -> !encontrada.getId().equals(idAtual)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
  }

  private Empresa from(Empresa.Dados dados) {
    try {
      return new Empresa(dados);
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
    }
  }
}
