package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService
{

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal)
	{
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId)
	{
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel())
		{
			throw new IllegalStateException("Itens fora de estoque.");
		}

		BigDecimal custoTotal = calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado())
		{
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso())
		{
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	} 

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho, Regiao regiao, TipoCliente tipoCliente)
	{
        BigDecimal totalCarrinho = calcularTotalCarrinho(carrinho);
        BigDecimal totalFrete = calcularFrete(carrinho);
        BigDecimal custoTotal = totalCarrinho.add(totalFrete);

		return totalCarrinho.add(custoTotal);

	}

    private BigDecimal calcularFrete(CarrinhoDeCompras carrinho) {
        BigDecimal adicionalProdutosFrageis =
                carrinho.getItens().stream().map(
                itemCompra -> {
                        if(itemCompra.getProduto().isFragil()){
                            return BigDecimal.valueOf(itemCompra.getQuantidade());
                        }else{
                            return BigDecimal.ZERO;
                        }
                    }
                ).reduce(BigDecimal.ZERO,BigDecimal::add).multiply(BigDecimal.valueOf(5));


        BigDecimal pesoTotal = carrinho.getItens().stream().map(
                itemCompra -> itemCompra
                        .getProduto()
                        .getPesoFisico()
                        .multiply(BigDecimal.valueOf(itemCompra.getQuantidade()))).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal multiplicadorPeso;

        if(pesoTotal.compareTo(BigDecimal.ZERO) <= 0){
            throw  new RuntimeException();
        }else if(pesoTotal.compareTo(BigDecimal.valueOf(5))<=0){
            multiplicadorPeso = BigDecimal.valueOf(1);
        }else if(pesoTotal.compareTo(BigDecimal.valueOf(5))>0 && pesoTotal.compareTo(BigDecimal.valueOf(10))<=0){
            multiplicadorPeso = BigDecimal.valueOf(2);
        }else if(pesoTotal.compareTo(BigDecimal.valueOf(10))>0 && pesoTotal.compareTo(BigDecimal.valueOf(50))<=0){
            multiplicadorPeso = BigDecimal.valueOf(4);
        }else{
            multiplicadorPeso = BigDecimal.valueOf(7);
        }

        BigDecimal freteTotal = pesoTotal.multiply(multiplicadorPeso).add(adicionalProdutosFrageis);
        return freteTotal;
    }

    private BigDecimal calcularTotalCarrinho(CarrinhoDeCompras carrinho) {
        BigDecimal totalBruto = carrinho.getItens().stream().map(items ->
                items.getProduto().getPreco().multiply(BigDecimal.valueOf(items.getQuantidade()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal descontoBase;
        if(totalBruto.compareTo(BigDecimal.valueOf(1000))>=0){
            descontoBase = BigDecimal.valueOf(20);
        }else if(totalBruto.compareTo(BigDecimal.valueOf(500))>=0 && totalBruto.compareTo(BigDecimal.valueOf(1000))<0){
            descontoBase = BigDecimal.valueOf(10);
        }else{
            descontoBase = BigDecimal.valueOf(0);
        }

        BigDecimal descontoTotal = descontoBase.divide(BigDecimal.valueOf(100),2,BigDecimal.ROUND_HALF_UP).multiply(totalBruto);
        return totalBruto.subtract(descontoTotal);

    }
}
