package ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.external.fake.EstoqueSimulado;
import ecommerce.external.fake.PagamentoSimulado;
import ecommerce.repository.CarrinhoDeComprasRepository;
import ecommerce.repository.ClienteRepository;

/**
 * Cenário 1: Fakes para IEstoqueExternal e IPagamentoExternal
 * Mocks para as dependências da camada Repository
 */
@DisplayName("Cenário 1 - Testes de finalizarCompra com Fakes (External) e Mocks (Repository)")
public class CompraServiceCenario1Test
{
	private CompraService compraService;
	private CarrinhoDeComprasService carrinhoService;
	private ClienteService clienteService;
	
	// Fakes para serviços externos
	private EstoqueSimulado estoqueExternal;
	private PagamentoSimulado pagamentoExternal;
	
	// Mocks para repositories
	private CarrinhoDeComprasRepository carrinhoRepository;
	private ClienteRepository clienteRepository;

	@BeforeEach
	void setUp()
	{
		// Cria os fakes para serviços externos
		estoqueExternal = new EstoqueSimulado();
		pagamentoExternal = new PagamentoSimulado();
		
		// Cria os mocks para repositories
		carrinhoRepository = mock(CarrinhoDeComprasRepository.class);
		clienteRepository = mock(ClienteRepository.class);
		
		// Cria os serviços com as dependências
		carrinhoService = new CarrinhoDeComprasService(carrinhoRepository);
		clienteService = new ClienteService(clienteRepository);
		compraService = new CompraService(carrinhoService, clienteService, estoqueExternal, pagamentoExternal);
	}

	@Test
	@DisplayName("Deve finalizar compra com sucesso quando há estoque e pagamento autorizado")
	void deveFinalizarCompraComSucesso()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "João Silva", Regiao.NORDESTE, TipoCliente.OURO);
		
		Produto produto1 = new Produto(1L, "Notebook", "Notebook Dell", 
				BigDecimal.valueOf(2000.00), BigDecimal.valueOf(2.5), 
				null, null, null, false, TipoProduto.ELETRONICO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 2L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		
		// Configura estoque (produto 1 com 100 unidades)
		estoqueExternal.definirEstoque(1L, 100L);
		
		// Configura limite de crédito do cliente
		pagamentoExternal.definirLimiteCredito(clienteId, 10000.0);
		
		// Mock dos repositories
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(carrinhoRepository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.of(carrinho));
		
		// Act
		CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
		
		// Assert
		assertThat(resultado.sucesso()).isTrue();
		assertThat(resultado.transacaoPagamentoId()).isNotNull();
		assertThat(resultado.mensagem()).isEqualTo("Compra finalizada com sucesso.");
		
		// Verifica que o estoque foi reduzido
		assertThat(estoqueExternal.consultarEstoque(1L)).isEqualTo(98L);
		
		// Verifica que o pagamento foi autorizado
		assertThat(pagamentoExternal.verificarPagamentoAutorizado(resultado.transacaoPagamentoId())).isTrue();
		
		// Verifica as chamadas aos mocks
		verify(clienteRepository, times(1)).findById(clienteId);
		verify(carrinhoRepository, times(1)).findByIdAndCliente(carrinhoId, cliente);
	}

	@Test
	@DisplayName("Deve lançar exceção quando itens estão fora de estoque")
	void deveLancarExcecaoQuandoItensForaDeEstoque()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Maria Santos", Regiao.SUL, TipoCliente.PRATA);
		
		Produto produto1 = new Produto(5L, "Produto Indisponível", "Sem estoque", 
				BigDecimal.valueOf(100.00), BigDecimal.valueOf(1.0), 
				null, null, null, false, TipoProduto.ELETRONICO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 5L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		
		// Produto 5 tem estoque zero no EstoqueSimulado
		
		// Mock dos repositories
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(carrinhoRepository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.of(carrinho));
		
		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Itens fora de estoque.");
		
		// Verifica que os repositories foram chamados
		verify(clienteRepository, times(1)).findById(clienteId);
		verify(carrinhoRepository, times(1)).findByIdAndCliente(carrinhoId, cliente);
	}

	@Test
	@DisplayName("Deve lançar exceção quando pagamento não é autorizado")
	void deveLancarExcecaoQuandoPagamentoNaoAutorizado()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 3L; // Cliente com limite baixo
		
		Cliente cliente = new Cliente(clienteId, "Pedro Costa", Regiao.NORDESTE, TipoCliente.BRONZE);
		
		Produto produto1 = new Produto(1L, "Produto Caro", "Acima do limite", 
				BigDecimal.valueOf(5000.00), BigDecimal.valueOf(1.0), 
				null, null, null, false, TipoProduto.ELETRONICO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		
		// Configura estoque suficiente
		estoqueExternal.definirEstoque(1L, 100L);
		
		// Cliente 3 tem limite de crédito de apenas 100.0
		
		// Mock dos repositories
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(carrinhoRepository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.of(carrinho));
		
		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Pagamento não autorizado.");
		
		// Verifica que o estoque NÃO foi alterado
		assertThat(estoqueExternal.consultarEstoque(1L)).isEqualTo(100L);
	}

	@Test
	@DisplayName("Deve cancelar pagamento quando baixa no estoque falha")
	void deveCancelarPagamentoQuandoBaixaNoEstoqueFalha()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Ana Oliveira", Regiao.SUDESTE, TipoCliente.OURO);
		
		Produto produto1 = new Produto(1L, "Produto", "Descrição", 
				BigDecimal.valueOf(100.00), BigDecimal.valueOf(1.0), 
				null, null, null, false, TipoProduto.ALIMENTO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 10L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		
		// Cria um EstoqueSimulado customizado que simula race condition
		// Disponibilidade retorna OK, mas darBaixa falha
		EstoqueSimulado estoqueComRaceCondition = new EstoqueSimulado() {
			private int chamadas = 0;
			
			@Override
			public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades) {
				// Primeira chamada: retorna disponível
				return new DisponibilidadeDTO(true, new ArrayList<>());
			}
			
			@Override
			public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades) {
				// Simula que outro processo consumiu o estoque entre a verificação e a baixa
				return new EstoqueBaixaDTO(false);
			}
		};
		
		// Recria o serviço com o estoque customizado
		carrinhoService = new CarrinhoDeComprasService(carrinhoRepository);
		clienteService = new ClienteService(clienteRepository);
		compraService = new CompraService(carrinhoService, clienteService, estoqueComRaceCondition, pagamentoExternal);
		
		// Mock dos repositories
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(carrinhoRepository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.of(carrinho));
		
		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Erro ao dar baixa no estoque.");
	}

	@Test
	@DisplayName("Deve calcular custo total corretamente com múltiplos itens")
	void deveCalcularCustoTotalComMultiplosItens()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Carlos Lima", Regiao.NORTE, TipoCliente.PRATA);
		
		// Produto 1: R$ 100,00 x 5 = R$ 500,00
		Produto produto1 = new Produto(1L, "Produto A", "Desc A", 
				BigDecimal.valueOf(100.00), BigDecimal.valueOf(1.0), 
				null, null, null, false, TipoProduto.ALIMENTO);
		
		// Produto 2: R$ 200,00 x 3 = R$ 600,00
		Produto produto2 = new Produto(2L, "Produto B", "Desc B", 
				BigDecimal.valueOf(200.00), BigDecimal.valueOf(2.0), 
				null, null, null, false, TipoProduto.ELETRONICO);
		
		// Total: R$ 1100,00 (desconto de 20% = R$ 880,00)
		// Frete: 11 kg (5kg produto1 + 6kg produto2) = 11 * 4 = R$ 44,00
		// Total Final: R$ 924,00
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 5L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 3L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		itens.add(item2);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		
		// Configura estoque suficiente
		estoqueExternal.definirEstoque(1L, 100L);
		estoqueExternal.definirEstoque(2L, 100L);
		
		// Configura limite de crédito
		pagamentoExternal.definirLimiteCredito(clienteId, 10000.0);
		
		// Mock dos repositories
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(carrinhoRepository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.of(carrinho));
		
		// Act
		CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
		
		// Assert
		assertThat(resultado.sucesso()).isTrue();
		assertThat(resultado.transacaoPagamentoId()).isNotNull();
		
		// Verifica que os estoques foram reduzidos
		assertThat(estoqueExternal.consultarEstoque(1L)).isEqualTo(95L);
		assertThat(estoqueExternal.consultarEstoque(2L)).isEqualTo(97L);
	}

	@Test
	@DisplayName("Deve processar compra com produto frágil corretamente")
	void deveProcessarCompraComProdutoFragil()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Fernanda Rocha", Regiao.CENTRO_OESTE, TipoCliente.OURO);
		
		// Produto frágil: adicional de R$ 5,00 por unidade no frete
		Produto produtoFragil = new Produto(1L, "Vidro", "Produto frágil", 
				BigDecimal.valueOf(50.00), BigDecimal.valueOf(2.0), 
				null, null, null, true, TipoProduto.MOVEL);
		
		ItemCompra item1 = new ItemCompra(1L, produtoFragil, 3L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		
		// Configura estoque
		estoqueExternal.definirEstoque(1L, 100L);
		
		// Configura limite de crédito
		pagamentoExternal.definirLimiteCredito(clienteId, 5000.0);
		
		// Mock dos repositories
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(carrinhoRepository.findByIdAndCliente(carrinhoId, cliente)).thenReturn(Optional.of(carrinho));
		
		// Act
		CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
		
		// Assert
		assertThat(resultado.sucesso()).isTrue();
		assertThat(estoqueExternal.consultarEstoque(1L)).isEqualTo(97L);
	}
}
