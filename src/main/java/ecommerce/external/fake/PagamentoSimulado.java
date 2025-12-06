package ecommerce.external.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

@Service
public class PagamentoSimulado implements IPagamentoExternal
{
	private AtomicLong contadorTransacoes = new AtomicLong(1000);
	private Map<Long, Double> limiteCredito = new HashMap<>();
	private Map<Long, Boolean> pagamentosAutorizados = new HashMap<>();

	public PagamentoSimulado()
	{
		// Inicializa alguns clientes com limites de crédito
		limiteCredito.put(1L, 5000.0);
		limiteCredito.put(2L, 1000.0);
		limiteCredito.put(3L, 100.0);
		limiteCredito.put(4L, 10000.0);
	}

	public void definirLimiteCredito(Long clienteId, Double limite)
	{
		limiteCredito.put(clienteId, limite);
	}

	public void removerLimiteCredito(Long clienteId)
	{
		limiteCredito.remove(clienteId);
	}

	@Override
	public PagamentoDTO autorizarPagamento(Long clienteId, Double custoTotal)
	{
		if (clienteId == null || custoTotal == null || custoTotal <= 0)
		{
			return new PagamentoDTO(false, null);
		}

		// Verifica se o cliente tem limite de crédito suficiente
		Double limite = limiteCredito.getOrDefault(clienteId, Double.MAX_VALUE);

		if (custoTotal > limite)
		{
			return new PagamentoDTO(false, null);
		}

		// Gera um ID único para a transação
		Long transacaoId = contadorTransacoes.incrementAndGet();
		pagamentosAutorizados.put(transacaoId, true);

		return new PagamentoDTO(true, transacaoId);
	}

	@Override
	public void cancelarPagamento(Long clienteId, Long pagamentoTransacaoId)
	{
		if (pagamentoTransacaoId != null)
		{
			pagamentosAutorizados.put(pagamentoTransacaoId, false);
		}
	}

	public boolean verificarPagamentoAutorizado(Long transacaoId)
	{
		return pagamentosAutorizados.getOrDefault(transacaoId, false);
	}
}
