package ecommerce.repository.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.repository.CarrinhoDeComprasRepository;

/**
 * Implementação fake do CarrinhoDeComprasRepository para testes
 */
public class CarrinhoDeComprasRepositoryFake implements CarrinhoDeComprasRepository
{
	private Map<Long, CarrinhoDeCompras> carrinhos = new HashMap<>();
	private Long proximoId = 1L;

	public CarrinhoDeComprasRepositoryFake()
	{
		// Inicializa vazio
	}

	public void adicionarCarrinho(CarrinhoDeCompras carrinho)
	{
		if (carrinho.getId() == null)
		{
			carrinho.setId(proximoId++);
		}
		carrinhos.put(carrinho.getId(), carrinho);
	}

	public void limpar()
	{
		carrinhos.clear();
		proximoId = 1L;
	}

	@Override
	public Optional<CarrinhoDeCompras> findByIdAndCliente(Long carrinhoId, Cliente cliente)
	{
		CarrinhoDeCompras carrinho = carrinhos.get(carrinhoId);
		if (carrinho != null && carrinho.getCliente().getId().equals(cliente.getId()))
		{
			return Optional.of(carrinho);
		}
		return Optional.empty();
	}

	@Override
	public Optional<CarrinhoDeCompras> findById(Long id)
	{
		return Optional.ofNullable(carrinhos.get(id));
	}

	@Override
	public CarrinhoDeCompras save(CarrinhoDeCompras carrinho)
	{
		if (carrinho.getId() == null)
		{
			carrinho.setId(proximoId++);
		}
		carrinhos.put(carrinho.getId(), carrinho);
		return carrinho;
	}

	@Override
	public void deleteById(Long id)
	{
		carrinhos.remove(id);
	}

	@Override
	public boolean existsById(Long id)
	{
		return carrinhos.containsKey(id);
	}

	@Override
	public long count()
	{
		return carrinhos.size();
	}

	@Override
	public void delete(CarrinhoDeCompras entity)
	{
		carrinhos.remove(entity.getId());
	}

	@Override
	public void deleteAll()
	{
		carrinhos.clear();
	}

	@Override
	public void deleteAll(Iterable<? extends CarrinhoDeCompras> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteAllById(Iterable<? extends Long> ids)
	{
		ids.forEach(this::deleteById);
	}

	@Override
	public <S extends CarrinhoDeCompras> List<S> saveAll(Iterable<S> entities)
	{
		entities.forEach(this::save);
		return (List<S>) entities;
	}

	@Override
	public List<CarrinhoDeCompras> findAll()
	{
		return new ArrayList<>(carrinhos.values());
	}

	@Override
	public List<CarrinhoDeCompras> findAllById(Iterable<Long> ids)
	{
		return carrinhos.values().stream()
				.filter(c -> {
					for (Long id : ids)
					{
						if (c.getId().equals(id))
							return true;
					}
					return false;
				})
				.toList();
	}

	@Override
	public void flush()
	{
		// Não faz nada em um fake
	}

	@Override
	public <S extends CarrinhoDeCompras> S saveAndFlush(S entity)
	{
		return (S) save(entity);
	}

	@Override
	public <S extends CarrinhoDeCompras> List<S> saveAllAndFlush(Iterable<S> entities)
	{
		return saveAll(entities);
	}

	@Override
	public void deleteAllInBatch()
	{
		deleteAll();
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Long> ids)
	{
		deleteAllById(ids);
	}

	@Override
	public void deleteAllInBatch(Iterable<CarrinhoDeCompras> entities)
	{
		deleteAll(entities);
	}

	@Override
	public CarrinhoDeCompras getOne(Long id)
	{
		return findById(id).orElse(null);
	}

	@Override
	public CarrinhoDeCompras getById(Long id)
	{
		return findById(id).orElse(null);
	}

	@Override
	public CarrinhoDeCompras getReferenceById(Long id)
	{
		return findById(id).orElse(null);
	}

	@Override
	public <S extends CarrinhoDeCompras> Optional<S> findOne(org.springframework.data.domain.Example<S> example)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends CarrinhoDeCompras> List<S> findAll(org.springframework.data.domain.Example<S> example)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends CarrinhoDeCompras> List<S> findAll(org.springframework.data.domain.Example<S> example,
			org.springframework.data.domain.Sort sort)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends CarrinhoDeCompras> org.springframework.data.domain.Page<S> findAll(
			org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends CarrinhoDeCompras> long count(org.springframework.data.domain.Example<S> example)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends CarrinhoDeCompras> boolean exists(org.springframework.data.domain.Example<S> example)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public List<CarrinhoDeCompras> findAll(org.springframework.data.domain.Sort sort)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public org.springframework.data.domain.Page<CarrinhoDeCompras> findAll(
			org.springframework.data.domain.Pageable pageable)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends CarrinhoDeCompras, R> R findBy(org.springframework.data.domain.Example<S> example,
			java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}
}
