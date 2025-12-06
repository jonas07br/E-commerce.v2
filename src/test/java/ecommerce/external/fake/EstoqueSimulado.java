package ecommerce.external.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.external.IEstoqueExternal;

@Service
public class EstoqueSimulado implements IEstoqueExternal
{
	private Map<Long, Long> estoque = new HashMap<>();

	public EstoqueSimulado()
	{
		// Inicializa estoque com alguns produtos
		estoque.put(1L, 100L);
		estoque.put(2L, 50L);
		estoque.put(3L, 200L);
		estoque.put(4L, 10L);
		estoque.put(5L, 0L); // Produto sem estoque
	}

	public void adicionarEstoque(Long produtoId, Long quantidade)
	{
		estoque.put(produtoId, estoque.getOrDefault(produtoId, 0L) + quantidade);
	}

	public void definirEstoque(Long produtoId, Long quantidade)
	{
		estoque.put(produtoId, quantidade);
	}

	public Long consultarEstoque(Long produtoId)
	{
		return estoque.getOrDefault(produtoId, 0L);
	}

	@Override
	public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades)
	{
		if (produtosIds == null || produtosQuantidades == null || produtosIds.size() != produtosQuantidades.size())
		{
			return new EstoqueBaixaDTO(false);
		}

		// Verifica se há estoque suficiente antes de dar baixa
		for (int i = 0; i < produtosIds.size(); i++)
		{
			Long produtoId = produtosIds.get(i);
			Long quantidade = produtosQuantidades.get(i);
			Long estoqueAtual = estoque.getOrDefault(produtoId, 0L);

			if (estoqueAtual < quantidade)
			{
				return new EstoqueBaixaDTO(false);
			}
		}

		// Dá baixa no estoque
		for (int i = 0; i < produtosIds.size(); i++)
		{
			Long produtoId = produtosIds.get(i);
			Long quantidade = produtosQuantidades.get(i);
			estoque.put(produtoId, estoque.get(produtoId) - quantidade);
		}

		return new EstoqueBaixaDTO(true);
	}

	@Override
	public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades)
	{
		if (produtosIds == null || produtosQuantidades == null || produtosIds.size() != produtosQuantidades.size())
		{
			return new DisponibilidadeDTO(false, new ArrayList<>());
		}

		List<Long> produtosIndisponiveis = new ArrayList<>();

		for (int i = 0; i < produtosIds.size(); i++)
		{
			Long produtoId = produtosIds.get(i);
			Long quantidadeSolicitada = produtosQuantidades.get(i);
			Long estoqueDisponivel = estoque.getOrDefault(produtoId, 0L);

			if (estoqueDisponivel < quantidadeSolicitada)
			{
				produtosIndisponiveis.add(produtoId);
			}
		}

		boolean disponivel = produtosIndisponiveis.isEmpty();
		return new DisponibilidadeDTO(disponivel, produtosIndisponiveis);
	}
}
