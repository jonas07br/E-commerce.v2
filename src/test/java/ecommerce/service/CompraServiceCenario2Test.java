package ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.repository.fake.CarrinhoDeComprasRepositoryFake;
import ecommerce.repository.fake.ClienteRepositoryFake;

/**
 * Cenário 2: Mocks para IEstoqueExternal e IPagamentoExternal
 * Fakes para as dependências da camada Repository
 */
@DisplayName("Cenário 2 - Testes de finalizarCompra com Mocks (External) e Fakes (Repository)")
public class CompraServiceCenario2Test
{
	private CompraService compraService;
	private CarrinhoDeComprasService carrinhoService;
	private ClienteService clienteService;
	
	// Mocks para serviços externos
	private IEstoqueExternal estoqueExternal;
	private IPagamentoExternal pagamentoExternal;
	
	// Fakes para repositories
	private CarrinhoDeComprasRepositoryFake carrinhoRepository;
	private ClienteRepositoryFake clienteRepository;

	@BeforeEach
	void setUp()
	{
		// Cria os mocks para serviços externos
		estoqueExternal = mock(IEstoqueExternal.class);
		pagamentoExternal = mock(IPagamentoExternal.class);
		
		// Cria os fakes para repositories
		carrinhoRepository = new CarrinhoDeComprasRepositoryFake();
		clienteRepository = new ClienteRepositoryFake();
		
		// Limpa os repositórios antes de cada teste
		carrinhoRepository.limpar();
		clienteRepository.limpar();
		
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
		clienteRepository.adicionarCliente(cliente);
		
		Produto produto1 = new Produto(1L, "Notebook", "Notebook Dell", 
				BigDecimal.valueOf(2000.00), BigDecimal.valueOf(2.5), 
				null, null, null, false, TipoProduto.ELETRONICO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 2L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		carrinhoRepository.adicionarCarrinho(carrinho);
		
		// Configura comportamento dos mocks
		when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
			.thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
		
		when(pagamentoExternal.autorizarPagamento(eq(clienteId), any(Double.class)))
			.thenReturn(new PagamentoDTO(true, 1001L));
		
		when(estoqueExternal.darBaixa(anyList(), anyList()))
			.thenReturn(new EstoqueBaixaDTO(true));
		
		// Act
		CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
		
		// Assert
		assertThat(resultado.sucesso()).isTrue();
		assertThat(resultado.transacaoPagamentoId()).isEqualTo(1001L);
		assertThat(resultado.mensagem()).isEqualTo("Compra finalizada com sucesso.");
		
		// Verifica que os métodos foram chamados corretamente
		verify(estoqueExternal, times(1)).verificarDisponibilidade(anyList(), anyList());
		verify(pagamentoExternal, times(1)).autorizarPagamento(eq(clienteId), any(Double.class));
		verify(estoqueExternal, times(1)).darBaixa(anyList(), anyList());
		verify(pagamentoExternal, never()).cancelarPagamento(any(), any());
	}

	@Test
	@DisplayName("Deve lançar exceção quando itens estão fora de estoque")
	void deveLancarExcecaoQuandoItensForaDeEstoque()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Maria Santos", Regiao.SUL, TipoCliente.PRATA);
		clienteRepository.adicionarCliente(cliente);
		
		Produto produto1 = new Produto(5L, "Produto Indisponível", "Sem estoque", 
				BigDecimal.valueOf(100.00), BigDecimal.valueOf(1.0), 
				null, null, null, false, TipoProduto.ELETRONICO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 5L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		carrinhoRepository.adicionarCarrinho(carrinho);
		
		// Configura mock para retornar indisponibilidade
		List<Long> produtosIndisponiveis = new ArrayList<>();
		produtosIndisponiveis.add(5L);
		when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
			.thenReturn(new DisponibilidadeDTO(false, produtosIndisponiveis));
		
		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Itens fora de estoque.");
		
		// Verifica que verificarDisponibilidade foi chamado mas darBaixa não
		verify(estoqueExternal, times(1)).verificarDisponibilidade(anyList(), anyList());
		verify(pagamentoExternal, never()).autorizarPagamento(any(), any());
		verify(estoqueExternal, never()).darBaixa(anyList(), anyList());
	}

	@Test
	@DisplayName("Deve lançar exceção quando pagamento não é autorizado")
	void deveLancarExcecaoQuandoPagamentoNaoAutorizado()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 3L;
		
		Cliente cliente = new Cliente(clienteId, "Pedro Costa", Regiao.NORDESTE, TipoCliente.BRONZE);
		clienteRepository.adicionarCliente(cliente);
		
		Produto produto1 = new Produto(1L, "Produto Caro", "Acima do limite", 
				BigDecimal.valueOf(5000.00), BigDecimal.valueOf(1.0), 
				null, null, null, false, TipoProduto.ELETRONICO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 1L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		carrinhoRepository.adicionarCarrinho(carrinho);
		
		// Configura mocks
		when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
			.thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
		
		when(pagamentoExternal.autorizarPagamento(eq(clienteId), any(Double.class)))
			.thenReturn(new PagamentoDTO(false, null));
		
		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Pagamento não autorizado.");
		
		// Verifica que darBaixa não foi chamado
		verify(estoqueExternal, times(1)).verificarDisponibilidade(anyList(), anyList());
		verify(pagamentoExternal, times(1)).autorizarPagamento(eq(clienteId), any(Double.class));
		verify(estoqueExternal, never()).darBaixa(anyList(), anyList());
	}

	@Test
	@DisplayName("Deve cancelar pagamento quando baixa no estoque falha")
	void deveCancelarPagamentoQuandoBaixaNoEstoqueFalha()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Ana Oliveira", Regiao.SUDESTE, TipoCliente.OURO);
		clienteRepository.adicionarCliente(cliente);
		
		Produto produto1 = new Produto(1L, "Produto", "Descrição", 
				BigDecimal.valueOf(100.00), BigDecimal.valueOf(1.0), 
				null, null, null, false, TipoProduto.ALIMENTO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 10L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		carrinhoRepository.adicionarCarrinho(carrinho);
		
		// Configura mocks
		when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
			.thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
		
		when(pagamentoExternal.autorizarPagamento(eq(clienteId), any(Double.class)))
			.thenReturn(new PagamentoDTO(true, 2001L));
		
		// Simula falha na baixa do estoque
		when(estoqueExternal.darBaixa(anyList(), anyList()))
			.thenReturn(new EstoqueBaixaDTO(false));
		
		// Act & Assert
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Erro ao dar baixa no estoque.");
		
		// Verifica que o pagamento foi cancelado
		verify(estoqueExternal, times(1)).verificarDisponibilidade(anyList(), anyList());
		verify(pagamentoExternal, times(1)).autorizarPagamento(eq(clienteId), any(Double.class));
		verify(estoqueExternal, times(1)).darBaixa(anyList(), anyList());
		verify(pagamentoExternal, times(1)).cancelarPagamento(clienteId, 2001L);
	}

	@Test
	@DisplayName("Deve calcular custo total corretamente com múltiplos itens")
	void deveCalcularCustoTotalComMultiplosItens()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Carlos Lima", Regiao.NORTE, TipoCliente.PRATA);
		clienteRepository.adicionarCliente(cliente);
		
		// Produto 1: R$ 100,00 x 5 = R$ 500,00
		Produto produto1 = new Produto(1L, "Produto A", "Desc A", 
				BigDecimal.valueOf(100.00), BigDecimal.valueOf(1.0), 
				null, null, null, false, TipoProduto.ALIMENTO);
		
		// Produto 2: R$ 200,00 x 3 = R$ 600,00
		Produto produto2 = new Produto(2L, "Produto B", "Desc B", 
				BigDecimal.valueOf(200.00), BigDecimal.valueOf(2.0), 
				null, null, null, false, TipoProduto.ELETRONICO);
		
		ItemCompra item1 = new ItemCompra(1L, produto1, 5L);
		ItemCompra item2 = new ItemCompra(2L, produto2, 3L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		itens.add(item2);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		carrinhoRepository.adicionarCarrinho(carrinho);
		
		// Configura mocks
		when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
			.thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
		
		when(pagamentoExternal.autorizarPagamento(eq(clienteId), any(Double.class)))
			.thenReturn(new PagamentoDTO(true, 3001L));
		
		when(estoqueExternal.darBaixa(anyList(), anyList()))
			.thenReturn(new EstoqueBaixaDTO(true));
		
		// Act
		CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
		
		// Assert
		assertThat(resultado.sucesso()).isTrue();
		assertThat(resultado.transacaoPagamentoId()).isEqualTo(3001L);
		
		// Verifica todas as interações
		verify(estoqueExternal, times(1)).verificarDisponibilidade(anyList(), anyList());
		verify(pagamentoExternal, times(1)).autorizarPagamento(eq(clienteId), any(Double.class));
		verify(estoqueExternal, times(1)).darBaixa(anyList(), anyList());
	}

	@Test
	@DisplayName("Deve processar compra com produto frágil corretamente")
	void deveProcessarCompraComProdutoFragil()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Fernanda Rocha", Regiao.CENTRO_OESTE, TipoCliente.OURO);
		clienteRepository.adicionarCliente(cliente);
		
		// Produto frágil: adicional de R$ 5,00 por unidade no frete
		Produto produtoFragil = new Produto(1L, "Vidro", "Produto frágil", 
				BigDecimal.valueOf(50.00), BigDecimal.valueOf(2.0), 
				null, null, null, true, TipoProduto.MOVEL);
		
		ItemCompra item1 = new ItemCompra(1L, produtoFragil, 3L);
		
		List<ItemCompra> itens = new ArrayList<>();
		itens.add(item1);
		
		CarrinhoDeCompras carrinho = new CarrinhoDeCompras(carrinhoId, cliente, itens, LocalDate.now());
		carrinhoRepository.adicionarCarrinho(carrinho);
		
		// Configura mocks
		when(estoqueExternal.verificarDisponibilidade(anyList(), anyList()))
			.thenReturn(new DisponibilidadeDTO(true, new ArrayList<>()));
		
		when(pagamentoExternal.autorizarPagamento(eq(clienteId), any(Double.class)))
			.thenReturn(new PagamentoDTO(true, 4001L));
		
		when(estoqueExternal.darBaixa(anyList(), anyList()))
			.thenReturn(new EstoqueBaixaDTO(true));
		
		// Act
		CompraDTO resultado = compraService.finalizarCompra(carrinhoId, clienteId);
		
		// Assert
		assertThat(resultado.sucesso()).isTrue();
		assertThat(resultado.transacaoPagamentoId()).isEqualTo(4001L);
		
		// Verifica as interações
		verify(estoqueExternal, times(1)).verificarDisponibilidade(anyList(), anyList());
		verify(pagamentoExternal, times(1)).autorizarPagamento(eq(clienteId), any(Double.class));
		verify(estoqueExternal, times(1)).darBaixa(anyList(), anyList());
	}

	@Test
	@DisplayName("Deve lançar exceção quando cliente não existe")
	void deveLancarExcecaoQuandoClienteNaoExiste()
	{
		// Arrange
		Long carrinhoId = 1L;
		Long clienteId = 999L; // Cliente inexistente
		
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Cliente não encontrado");
		
		// Verifica que nenhum serviço externo foi chamado
		verify(estoqueExternal, never()).verificarDisponibilidade(anyList(), anyList());
		verify(pagamentoExternal, never()).autorizarPagamento(any(), any());
	}

	@Test
	@DisplayName("Deve lançar exceção quando carrinho não existe")
	void deveLancarExcecaoQuandoCarrinhoNaoExiste()
	{
		// Arrange
		Long carrinhoId = 999L; // Carrinho inexistente
		Long clienteId = 1L;
		
		Cliente cliente = new Cliente(clienteId, "Roberto Silva", Regiao.SUDESTE, TipoCliente.PRATA);
		clienteRepository.adicionarCliente(cliente);
		
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			compraService.finalizarCompra(carrinhoId, clienteId);
		});
		
		assertThat(exception.getMessage()).isEqualTo("Carrinho não encontrado.");
		
		// Verifica que nenhum serviço externo foi chamado
		verify(estoqueExternal, never()).verificarDisponibilidade(anyList(), anyList());
		verify(pagamentoExternal, never()).autorizarPagamento(any(), any());
	}
}
