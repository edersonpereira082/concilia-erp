package br.com.conciliacao.service;

import br.com.conciliacao.domain.Produto;
import br.com.conciliacao.domain.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProdutoService {
  private final ProdutoRepository repository;

  public ProdutoService(ProdutoRepository repository) { this.repository = repository; }

  public Produto criar(Produto produto) {
    if (repository.existsBySkuIgnoreCase(produto.getSku()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU já cadastrado");
    return repository.save(produto);
  }

  public List<Produto> listar() { return repository.findAll(); }

  public Produto buscar(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
  }

  public Produto atualizar(Long id, Produto dados) {
    Produto produto = buscar(id);
    if (repository.findBySkuIgnoreCase(dados.getSku()).filter(encontrado -> !encontrado.getId().equals(id)).isPresent())
      throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU já cadastrado");
    produto.atualizar(dados.getSku(), dados.getNome(), dados.getUnidade(), dados.getPrecoVenda(),
        dados.getPrecoCusto(), dados.getEstoque(), dados.getEstoqueMinimo());
    return repository.save(produto);
  }

  public Produto alterarAtivo(Long id, boolean ativo) {
    Produto produto = buscar(id);
    produto.alterarAtivo(ativo);
    return repository.save(produto);
  }

  public void excluir(Long id) {
    try {
      repository.delete(buscar(id));
    } catch (Exception exception) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto possui movimentos vinculados", exception);
    }
  }

  public Produto from(ProdutoRequest request) {
    return new Produto(request.sku(), request.nome(), request.unidade(),
        n(request.precoVenda()), n(request.precoCusto()), n(request.estoque()), n(request.estoqueMinimo()));
  }

  private BigDecimal n(BigDecimal valor) { return valor == null ? BigDecimal.ZERO : valor; }

  public record ProdutoRequest(String sku, String nome, String unidade, BigDecimal precoVenda,
      BigDecimal precoCusto, BigDecimal estoque, BigDecimal estoqueMinimo) {}
}
