package br.com.conciliacao.service;

import net.sf.jasperreports.engine.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;

@Service
public class BoletoReportService {
  public byte[] gerar(String beneficiario, String valor, LocalDate vencimento) throws Exception {
    var resource = new ClassPathResource("reports/boleto.jrxml");
    var report = JasperCompileManager.compileReport(resource.getInputStream());
    var params = new HashMap<String, Object>();
    params.put("beneficiario", beneficiario); params.put("valor", valor); params.put("vencimento", vencimento.toString());
    var print = JasperFillManager.fillReport(report, params, new JREmptyDataSource());
    var output = new ByteArrayOutputStream(); JasperExportManager.exportReportToPdfStream(print, output); return output.toByteArray();
  }
}
