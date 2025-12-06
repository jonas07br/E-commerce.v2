package ecommerce.external;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;

import java.util.List;

public class FakeEstoqueExternal implements IEstoqueExternal {
    @Override
    public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades) {
        return null;
    }

    @Override
    public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades) {
        return null;
    }
}
