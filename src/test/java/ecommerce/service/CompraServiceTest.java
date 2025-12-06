package ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ecommerce.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CompraServiceTest
{

    private static final BigDecimal PRECO_PADRAO_10 = BigDecimal.valueOf(10);
    private static final BigDecimal PRECO_PADRAO_1 = BigDecimal.valueOf(1);
    private static final BigDecimal PESO_PADRAO_10 = BigDecimal.valueOf(10);
    private static final BigDecimal PESO_PADRAO_1 = BigDecimal.valueOf(1);

    private CompraService compraService;

    private Produto produtoPadrao;

    @BeforeEach
    void setUp() {
        // Assume que CompraService requer injeção de dependência para outros serviços/repos
        compraService = new CompraService(null, null, null, null);

        produtoPadrao = new Produto(
                1L,"Laranja","Fruta boa",PRECO_PADRAO_10,PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO
        );

    }

	public void calcularCustoTotal()
	{
		CompraService service = new CompraService(null, null, null, null);

		CarrinhoDeCompras carrinho = new CarrinhoDeCompras();

		List<ItemCompra> itens = new ArrayList<>();

		ItemCompra item1 = new ItemCompra();
		ItemCompra item2 = new ItemCompra();
		ItemCompra item3 = new ItemCompra();
		// To-Do : falta setar os atributos dos itens
		itens.add(item1);
		itens.add(item2);
		itens.add(item3);
		carrinho.setItens(itens);

		BigDecimal custoTotal = service.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO);

		// Ao trabalhar com BigDecimal, evite comparar com equals() -- método que o
		// assertEquals usa,
		// pois ela leva em conta escala (ex: 10.0 != 10.00).
		// Use o método compareTo().
		BigDecimal esperado = new BigDecimal("0.00");
		assertEquals(0, custoTotal.compareTo(esperado), "Valor calculado incorreto: " + custoTotal);

		// Uma alternativa mais elegante, é usar a lib AssertJ
		// O método isEqualByComparingTo não leva em conta escala
		// e não precisa instanciar um BigDecimal para fazer a comparação
		assertThat(custoTotal).as("Custo Total da Compra").isEqualByComparingTo("0.0");
	}

    @Test
    public void deveLancarExcessaoPeso(){
        Produto produtoInvalido = new Produto(
                1L,"Laranja","Fruta boa",PRECO_PADRAO_10,null,null,null,null,false,TipoProduto.ALIMENTO
        );

        List<ItemCompra> itens = new ArrayList<>();
        ItemCompra item1 = new ItemCompra(1L,produtoInvalido,10L);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        assertThrows(RuntimeException.class,()->{
            compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO);
        });
    }

    @Test
    public void deveLancarExcessaoValorProduto(){
        BigDecimal valorInvalido = new BigDecimal(1L).negate();
        Produto produtoInvalido = new Produto(
                1L,"Laranja","Fruta boa",valorInvalido,PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO
        );

        List<ItemCompra> itens = new ArrayList<>();
        ItemCompra item1 = new ItemCompra(1L,produtoInvalido,10L);
        itens.add(item1);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        assertThrows(RuntimeException.class,()->{
            compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO);
        });
    }


    @ParameterizedTest
    @CsvSource({
//            VALOR , QUANTIDADE , TOTAL DO CARRINHO
            "10.00,1,10.00",
            "1000.00,1,800.00",
            "500.00,1,450.00"
    })
    public void deveCalcularCustoTotalDoCarrinho(BigDecimal valor, Long quantidade,BigDecimal valorTotal){
        Produto produto = new Produto(
                1L,"Produto basico",null,valor,PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,quantidade));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualTo(valorTotal);
    }

    @ParameterizedTest
    @CsvSource({
//            PESO , QUANTIDADE , TOTAL DO CARRINHO
            "5.00,1,0.00",
            "6.00,1,12.00",
            "11.00,1,44.00",
            "60.00,1,420.00"

    })
    public void deveCalcularFreteTotal(BigDecimal peso, Long quantidade,BigDecimal valorTotal){
        Produto produto = new Produto(
                1L,"Produto basico",null,BigDecimal.ZERO,peso,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,quantidade));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualTo(valorTotal);
    }

    @Test
    public void deveCalcularProdutosFrageis(){
        Produto produto = new Produto(
                1L,"Produto basico",null,PRECO_PADRAO_1,PESO_PADRAO_1,null,null,null,true,TipoProduto.ALIMENTO);
        ItemCompra item1 = new ItemCompra(1L,produto,5L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item1);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualTo("30.00");
    }

}
