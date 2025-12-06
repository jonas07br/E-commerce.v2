package ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

@DisplayName("Testes de CompraService - calcularCustoTotal")
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

    @Test
    @DisplayName("Deve lançar exceção quando peso é nulo ou inválido")
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
    @DisplayName("Deve lançar exceção quando valor do produto é negativo")
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
    @DisplayName("Deve calcular custo total do carrinho com descontos corretos")
    @CsvSource({
//            VALOR , QUANTIDADE , TOTAL DO CARRINHO
            "10.00,1,10.00",       // Sem desconto (< 500)
            "100.00,4,400.00",     // Sem desconto (400 < 500), sem frete (4kg)
            "500.00,1,450.00",     // 10% de desconto (500 -> 450) sem frete
            "999.99,1,899.99",     // 10% de desconto (limite inferior de 1000)
            "1000.00,1,800.00",    // 20% de desconto (1000 -> 800) sem frete
            "2000.00,1,1600.00"    // 20% de desconto (>= 1000)
    })
    public void deveCalcularCustoTotalDoCarrinho(BigDecimal valor, Long quantidade,BigDecimal valorTotal){
        Produto produto = new Produto(
                1L,"Produto basico",null,valor,PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,quantidade));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo(valorTotal);
    }

    @ParameterizedTest
    @DisplayName("Deve calcular frete baseado no peso total")
    @CsvSource({
//            PESO , QUANTIDADE , TOTAL (sem produto, só frete)
            "5.00,1,0.00",        // Faixa A: 0-5kg = isento
            "6.00,1,12.00",       // Faixa B: >5 e <=10kg = R$ 2/kg (6 * 2)
            "10.00,1,20.00",      // Faixa B: limite superior
            "11.00,1,44.00",      // Faixa C: >10 e <=50kg = R$ 4/kg (11 * 4)
            "50.00,1,200.00",     // Faixa C: limite superior (50 * 4)
            "60.00,1,420.00"      // Faixa D: >50kg = R$ 7/kg (60 * 7)

    })
    public void deveCalcularFreteTotal(BigDecimal peso, Long quantidade,BigDecimal valorTotal){
        Produto produto = new Produto(
                1L,"Produto basico",null,BigDecimal.ZERO,peso,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,quantidade));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo(valorTotal);
    }

    @Test
    @DisplayName("Deve adicionar R$ 5,00 por unidade frágil ao frete")
    public void deveCalcularProdutosFrageis(){
        Produto produto = new Produto(
                1L,"Produto basico",null,PRECO_PADRAO_1,PESO_PADRAO_1,null,null,null,true,TipoProduto.ALIMENTO);
        ItemCompra item1 = new ItemCompra(1L,produto,5L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item1);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // 5 unidades * R$ 1 = R$ 5 (sem desconto)
        // 5kg = isento de frete base
        // 5 unidades frágeis * R$ 5 = R$ 25
        // Total: R$ 5 + R$ 25 = R$ 30
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente com peso no limite da faixa A (exatamente 5kg)")
    public void deveCalcularComPesoLimiteFaixaA(){
        Produto produto = new Produto(
                1L,"Produto limite",null,BigDecimal.valueOf(100),BigDecimal.valueOf(5),null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // Peso = 5kg (isento de frete)
        // Valor = R$ 100 (sem desconto)
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente com peso logo acima da faixa A (5.1kg)")
    public void deveCalcularComPesoAcimaFaixaA(){
        Produto produto = new Produto(
                1L,"Produto acima",null,BigDecimal.ZERO,BigDecimal.valueOf(5.1),null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // Peso = 5.1kg -> Faixa B (R$ 2/kg)
        // Frete = 5.1 * 2 = 10.20
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("10.20");
    }

    @Test
    @DisplayName("Deve calcular corretamente múltiplos produtos com diferentes características")
    public void deveCalcularMultiplosProdutosComplexo(){
        // Produto 1: R$ 300 * 2 = R$ 600, 3kg * 2 = 6kg, não frágil
        Produto produto1 = new Produto(
                1L,"Produto A",null,BigDecimal.valueOf(300),BigDecimal.valueOf(3),null,null,null,false,TipoProduto.ALIMENTO);
        
		// Produto 2: R$ 200 * 3 = R$ 600, 2kg * 3 = 6kg, frágil (3 unidades * R$ 5 = R$ 15)
		Produto produto2 = new Produto(
				2L,"Produto B",null,BigDecimal.valueOf(200),BigDecimal.valueOf(2),null,null,null,true,TipoProduto.MOVEL);        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto1,2L));
        itens.add(new ItemCompra(2L,produto2,3L));
        
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        
        // Total bruto: R$ 600 + R$ 600 = R$ 1200
        // Desconto de 20%: R$ 1200 * 0.8 = R$ 960
        // Peso total: 6kg + 6kg = 12kg -> Faixa C (R$ 4/kg) = 12 * 4 = R$ 48
        // Adicional frágil: 3 * R$ 5 = R$ 15
        // Total: R$ 960 + R$ 48 + R$ 15 = R$ 1023.00
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("1023.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente no limite exato de R$ 500 (desconto de 10%)")
    public void deveCalcularLimiteExato500(){
        Produto produto = new Produto(
                1L,"Produto 500",null,BigDecimal.valueOf(500),PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 500 com desconto de 10% = R$ 450
        // Peso 1kg = isento
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("450.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente no limite exato de R$ 1000 (desconto de 20%)")
    public void deveCalcularLimiteExato1000(){
        Produto produto = new Produto(
                1L,"Produto 1000",null,BigDecimal.valueOf(1000),PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 1000 com desconto de 20% = R$ 800
        // Peso 1kg = isento
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente com peso de 10kg exatos (final da faixa B)")
    public void deveCalcularComPeso10kgExatos(){
        Produto produto = new Produto(
                1L,"Produto 10kg",null,BigDecimal.valueOf(100),BigDecimal.valueOf(10),null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 100 sem desconto
        // Peso 10kg: multiplicador 2, frete = 10 * 2 = R$ 20
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("120.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente com peso de 50kg exatos (final da faixa C)")
    public void deveCalcularComPeso50kgExatos(){
        Produto produto = new Produto(
                1L,"Produto 50kg",null,BigDecimal.valueOf(100),BigDecimal.valueOf(50),null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 100 sem desconto
        // Peso 50kg: multiplicador 4, frete = 50 * 4 = R$ 200
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente com R$ 499 (logo abaixo de 500)")
    public void deveCalcularCom499Reais(){
        Produto produto = new Produto(
                1L,"Produto 499",null,BigDecimal.valueOf(499),PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 499 sem desconto
        // Peso 1kg = isento
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("499.00");
    }

    @Test
    @DisplayName("Deve calcular corretamente com R$ 999 (logo abaixo de 1000)")
    public void deveCalcularCom999Reais(){
        Produto produto = new Produto(
                1L,"Produto 999",null,BigDecimal.valueOf(999),PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 999 com desconto de 10% = R$ 899.10
        // Peso 1kg = isento
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("899.10");
    }

    @Test
    @DisplayName("Deve calcular corretamente com peso 5.01kg (logo acima do limite 5kg)")
    public void deveCalcularComPeso501kg(){
        Produto produto = new Produto(
                1L,"Produto 5.01kg",null,BigDecimal.valueOf(100),BigDecimal.valueOf(5.01),null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 100 sem desconto
        // Peso 5.01kg: multiplicador 2, frete = 5.01 * 2 = R$ 10.02
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("110.02");
    }

    @Test
    @DisplayName("Deve calcular corretamente com peso 10.01kg (logo acima do limite 10kg)")
    public void deveCalcularComPeso1001kg(){
        Produto produto = new Produto(
                1L,"Produto 10.01kg",null,BigDecimal.valueOf(100),BigDecimal.valueOf(10.01),null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 100 sem desconto
        // Peso 10.01kg: multiplicador 4, frete = 10.01 * 4 = R$ 40.04
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("140.04");
    }

    @Test
    @DisplayName("Deve calcular corretamente com peso 50.01kg (logo acima do limite 50kg)")
    public void deveCalcularComPeso5001kg(){
        Produto produto = new Produto(
                1L,"Produto 50.01kg",null,BigDecimal.valueOf(100),BigDecimal.valueOf(50.01),null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 100 sem desconto
        // Peso 50.01kg: multiplicador 7, frete = 50.01 * 7 = R$ 350.07
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("450.07");
    }

    @Test
    @DisplayName("Deve calcular corretamente com R$ 500.01 (logo acima do limite 500)")
    public void deveCalcularCom50001Reais(){
        Produto produto = new Produto(
                1L,"Produto 500.01",null,BigDecimal.valueOf(500.01),PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 500.01 com desconto de 10% = R$ 450.01
        // Peso 1kg = isento
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("450.01");
    }

    @Test
    @DisplayName("Deve calcular corretamente com R$ 1000.01 (logo acima do limite 1000)")
    public void deveCalcularCom100001Reais(){
        Produto produto = new Produto(
                1L,"Produto 1000.01",null,BigDecimal.valueOf(1000.01),PESO_PADRAO_1,null,null,null,false,TipoProduto.ALIMENTO);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(new ItemCompra(1L,produto,1L));
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(itens);
        // R$ 1000.01 com desconto de 20% = R$ 800.01
        // Peso 1kg = isento
        assertThat(compraService.calcularCustoTotal(carrinho, Regiao.NORDESTE, TipoCliente.OURO)).isEqualByComparingTo("800.01");
    }

}

