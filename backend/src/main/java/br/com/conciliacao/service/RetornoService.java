package br.com.conciliacao.service;

import br.com.conciliacao.domain.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class RetornoService {
  private final RetornoRepository repository;
  private final RetornoDetalheRepository detalheRepository;
  private final RabbitTemplate rabbitTemplate;
  private final CnabParser parser;
  public RetornoService(RetornoRepository repository, RetornoDetalheRepository detalheRepository,
      RabbitTemplate rabbitTemplate, CnabParser parser) {
    this.repository = repository;
    this.detalheRepository = detalheRepository;
    this.rabbitTemplate = rabbitTemplate;
    this.parser = parser;
  }
  public RetornoBancario receber(MultipartFile arquivo) throws IOException {
    var nomeArquivo = arquivo == null ? null : arquivo.getOriginalFilename();
    if (arquivo == null || arquivo.isEmpty() || nomeArquivo == null || nomeArquivo.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo de retorno vazio ou sem nome");
    }
    var linhas = new String(arquivo.getBytes(), StandardCharsets.UTF_8).lines().toList();
    var detalhes = parser.parse(linhas);
    var retorno = repository.save(new RetornoBancario(nomeArquivo, LocalDate.now(), detalhes.size()));
    for (var detalhe : detalhes) {
      detalheRepository.save(new RetornoDetalhe(retorno, detalhe.numeroLinha(), detalhe.layout(), detalhe.linha(),
        detalhe.nossoNumero(), detalhe.codigoOcorrencia(), detalhe.valor(), detalhe.dataOcorrencia(),
        detalhe.resultado(), detalhe.motivo()));
    }
    rabbitTemplate.convertAndSend("retorno-bancario", retorno.getId().toString());
    return retorno;
  }
  public List<RetornoBancario> listar() { return repository.findAll(); }
  public List<RetornoDetalhe> listarDetalhes(Long id) { return detalheRepository.findByRetornoIdOrderByNumeroLinha(id); }
  public RetornoBancario buscar(Long id) { return repository.findById(Objects.requireNonNull(id, "id"))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Retorno não encontrado")); }
  public void processar(Long id) { var retorno = buscar(id); retorno.processar(); repository.save(retorno); }
  public RetornoBancario atualizar(Long id, String nomeArquivo) {
    if (nomeArquivo == null || nomeArquivo.isBlank())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do arquivo é obrigatório");
    var retorno = buscar(id);
    retorno.atualizarNome(nomeArquivo.trim());
    return repository.save(retorno);
  }
  public RetornoBancario alterarAtivo(Long id, boolean ativo) {
    var retorno = buscar(id);
    retorno.alterarAtivo(ativo);
    return repository.save(retorno);
  }
  public void excluir(Long id) {
    var retorno = buscar(id);
    detalheRepository.deleteByRetornoId(retorno.getId());
    repository.delete(retorno);
  }
}
