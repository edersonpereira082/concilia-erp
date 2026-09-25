package br.com.conciliacao.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import br.com.conciliacao.domain.Empresa;
import br.com.conciliacao.domain.EmpresaRepository;
import br.com.conciliacao.domain.TipoEmpresa;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

class EmpresaServiceTest {
  private final EmpresaRepository repository = mock(EmpresaRepository.class);
  private final EmpresaService service = new EmpresaService(repository);

  @Test
  void deveCriarEmpresaQuandoCnpjAindaNaoExiste() {
    var dados = dados("12345678000199", TipoEmpresa.CLIENTE);
    when(repository.findByCnpj("12345678000199")).thenReturn(Optional.empty());
    when(repository.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));

    assertThat(service.criar(dados).getCnpj()).isEqualTo("12345678000199");
    verify(repository).save(any(Empresa.class));
  }

  @Test
  void deveRecusarCnpjDuplicado() {
    var dados = dados("12345678000199", TipoEmpresa.FORNECEDOR);
    Empresa existente = empresa("12345678000199", TipoEmpresa.FORNECEDOR);
    when(repository.findByCnpj("12345678000199")).thenReturn(Optional.of(existente));

    assertThatThrownBy(() -> service.criar(dados))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("CNPJ já cadastrado");
    verify(repository, never()).save(any());
  }

  @Test
  void deveListarClienteTambemComEmpresasDeAmbosPerfis() {
    Empresa cliente = empresa("12345678000199", TipoEmpresa.CLIENTE);
    Empresa ambos = empresa("98765432000188", TipoEmpresa.AMBOS);
    when(repository.findByTipoIn(List.of(TipoEmpresa.CLIENTE, TipoEmpresa.AMBOS)))
        .thenReturn(List.of(cliente, ambos));

    assertThat(service.listar(TipoEmpresa.CLIENTE)).containsExactly(cliente, ambos);
  }

  @Test
  void deveAtualizarSemPermitirCnpjDeOutraEmpresa() {
    Empresa atual = mock(Empresa.class);
    Empresa outra = mock(Empresa.class);
    when(atual.getId()).thenReturn(1L);
    when(outra.getId()).thenReturn(2L);
    var dados = dados("98765432000188", TipoEmpresa.AMBOS);
    when(repository.findById(1L)).thenReturn(Optional.of(atual));
    when(repository.findByCnpj("98765432000188")).thenReturn(Optional.of(outra));

    assertThatThrownBy(() -> service.atualizar(1L, dados))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("CNPJ já cadastrado");
  }

  private @NonNull Empresa empresa(String cnpj, TipoEmpresa tipo) {
    return new Empresa("Empresa Teste", "Teste", cnpj, tipo, "teste@exemplo.com", "4330330101", "São Paulo", "SP");
  }

  private Empresa.Dados dados(String cnpj, TipoEmpresa tipo) {
    return new Empresa.Dados(null, "Empresa Teste", "Teste", null, cnpj, null, null, tipo,
        "teste@exemplo.com", "4330330101", "43999991212", null, null, null, null, null, "São Paulo", "SP");
  }
}
