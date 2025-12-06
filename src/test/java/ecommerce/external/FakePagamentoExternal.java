package ecommerce.external;

import ecommerce.dto.PagamentoDTO;

public class FakePagamentoExternal implements IPagamentoExternal {

    @Override
    public PagamentoDTO autorizarPagamento(Long clienteId, Double custoTotal) {
        return null;
    }

    @Override
    public void cancelarPagamento(Long clienteId, Long pagamentoTransacaoId) {

    }
}
