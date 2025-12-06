package ecommerce.repository.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ecommerce.entity.Cliente;
import ecommerce.repository.ClienteRepository;

/**
 * Implementação fake do ClienteRepository para testes
 */
public class ClienteRepositoryFake implements ClienteRepository
{
	private Map<Long, Cliente> clientes = new HashMap<>();
	private Long proximoId = 1L;

	public ClienteRepositoryFake()
	{
		// Inicializa com alguns clientes de teste
	}

	public void adicionarCliente(Cliente cliente)
	{
		if (cliente.getId() == null)
		{
			cliente.setId(proximoId++);
		}
		clientes.put(cliente.getId(), cliente);
	}

	public void limpar()
	{
		clientes.clear();
		proximoId = 1L;
	}

	@Override
	public Optional<Cliente> findById(Long id)
	{
		return Optional.ofNullable(clientes.get(id));
	}

	@Override
	public Cliente save(Cliente cliente)
	{
		if (cliente.getId() == null)
		{
			cliente.setId(proximoId++);
		}
		clientes.put(cliente.getId(), cliente);
		return cliente;
	}

	@Override
	public void deleteById(Long id)
	{
		clientes.remove(id);
	}

	@Override
	public boolean existsById(Long id)
	{
		return clientes.containsKey(id);
	}

	@Override
	public long count()
	{
		return clientes.size();
	}

	@Override
	public void delete(Cliente entity)
	{
		clientes.remove(entity.getId());
	}

	@Override
	public void deleteAll()
	{
		clientes.clear();
	}

	@Override
	public void deleteAll(Iterable<? extends Cliente> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteAllById(Iterable<? extends Long> ids)
	{
		ids.forEach(this::deleteById);
	}

	@Override
	public <S extends Cliente> List<S> saveAll(Iterable<S> entities)
	{
		entities.forEach(this::save);
		return (List<S>) entities;
	}

	@Override
	public List<Cliente> findAll()
	{
		return new ArrayList<>(clientes.values());
	}

	@Override
	public List<Cliente> findAllById(Iterable<Long> ids)
	{
		return clientes.values().stream()
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
	public <S extends Cliente> S saveAndFlush(S entity)
	{
		return (S) save(entity);
	}

	@Override
	public <S extends Cliente> List<S> saveAllAndFlush(Iterable<S> entities)
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
	public void deleteAllInBatch(Iterable<Cliente> entities)
	{
		deleteAll(entities);
	}

	@Override
	public Cliente getOne(Long id)
	{
		return findById(id).orElse(null);
	}

	@Override
	public Cliente getById(Long id)
	{
		return findById(id).orElse(null);
	}

	@Override
	public Cliente getReferenceById(Long id)
	{
		return findById(id).orElse(null);
	}

	@Override
	public <S extends Cliente> Optional<S> findOne(org.springframework.data.domain.Example<S> example)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends Cliente> List<S> findAll(org.springframework.data.domain.Example<S> example)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends Cliente> List<S> findAll(org.springframework.data.domain.Example<S> example,
			org.springframework.data.domain.Sort sort)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends Cliente> org.springframework.data.domain.Page<S> findAll(
			org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends Cliente> long count(org.springframework.data.domain.Example<S> example)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends Cliente> boolean exists(org.springframework.data.domain.Example<S> example)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public List<Cliente> findAll(org.springframework.data.domain.Sort sort)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public org.springframework.data.domain.Page<Cliente> findAll(org.springframework.data.domain.Pageable pageable)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}

	@Override
	public <S extends Cliente, R> R findBy(org.springframework.data.domain.Example<S> example,
			java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction)
	{
		throw new UnsupportedOperationException("Método não implementado no fake");
	}
}
