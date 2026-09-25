package br.com.conciliacao.legacy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exemplo didatico de ponto de manutencao legado: action servlet-style. */
public class ConsultaBoletoAction {
  public String execute(HttpServletRequest request, HttpServletResponse response) {
    request.setAttribute("status", "Consulta realizada no sistema legado");
    return "/WEB-INF/jsp/consulta-boleto.jsp";
  }
}
