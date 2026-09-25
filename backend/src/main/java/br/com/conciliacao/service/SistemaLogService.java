package br.com.conciliacao.service;

import br.com.conciliacao.domain.SistemaLog;
import br.com.conciliacao.domain.SistemaLogRepository;
import br.com.conciliacao.domain.Usuario;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SistemaLogService {
  private final SistemaLogRepository repository;

  public SistemaLogService(SistemaLogRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public List<SistemaLog> listar(Usuario solicitante) {
    return repository.findBySoftwareHouse_IdOrderByIdDesc(solicitante.getSoftwareHouse().getId());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void registrar(Usuario sessao, Usuario confirmador, String metodo, String recurso, int statusHttp) {
    if (sessao == null || sessao.getSoftwareHouse() == null) return;
    var acao = acao(metodo, recurso);
    var descricao = descricao(acao, recurso);
    var confirmadorNome = confirmador == null ? null : confirmador.getNome();
    var confirmadorEmail = confirmador == null ? null : confirmador.getEmail();
    repository.save(new SistemaLog(sessao.getSoftwareHouse(), acao, metodo, recurso, descricao,
        sessao.getNome(), sessao.getEmail(), confirmadorNome, confirmadorEmail, statusHttp));
  }

  private String acao(String metodo, String recurso) {
    var path = recurso == null ? "" : recurso.toLowerCase();
    if (path.contains("/faturar")) return "FATURAR";
    if (path.contains("/cancelar")) return "CANCELAR";
    if (path.contains("/baixar")) return "BAIXAR";
    if (path.contains("/lancamentos")) return "LANCAMENTO";
    if (path.contains("/abrir")) return "ABRIR";
    if (path.contains("/fechar")) return "FECHAR";
    if (path.contains("/ativo")) return "SITUACAO";
    if (path.contains("/auth/login")) return "LOGIN";
    if (path.contains("/auth/logout")) return "SAIR";
    if (path.contains("/retornos") && "POST".equals(metodo) && !path.contains("/detalhes")) return "IMPORTAR";
    if ("DELETE".equals(metodo)) return "EXCLUIR";
    if ("PUT".equals(metodo) || "PATCH".equals(metodo)) return "ALTERAR";
    if ("POST".equals(metodo)) return "CRIAR";
    return metodo == null ? "ROTINA" : metodo;
  }

  private String descricao(String acao, String recurso) {
    var modulo = modulo(recurso);
    return acao + " · " + modulo;
  }

  private String modulo(String recurso) {
    var path = recurso == null ? "" : recurso;
    if (path.contains("/empresas")) return "Cliente/Fornecedor";
    if (path.contains("/usuarios")) return "Usuários";
    if (path.contains("/produtos")) return "Produtos";
    if (path.contains("/pedidos")) return "Pedidos";
    if (path.contains("/titulos")) return "Títulos";
    if (path.contains("/formas-pagamento")) return "Formas de pagamento";
    if (path.contains("/transportadoras")) return "Transportadoras";
    if (path.contains("/contas-correntes")) return "Contas correntes";
    if (path.contains("/caixas")) return "Caixa";
    if (path.contains("/retornos")) return "Conciliação";
    if (path.contains("/auth/login")) return "Acesso ao sistema";
    if (path.contains("/auth/logout")) return "Saída do sistema";
    return path;
  }
}
