package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karnaval.entidad.Cliente;
import com.karnaval.repositorio.ClienteRepository;

@Service
public class ClienteServiceImpl implements ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Override
	public Cliente agregar(Cliente entidad) {
		return clienteRepository.save(entidad);
	}

	@Override
	public List<Cliente> listarTodos() {
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscar(Long id) {
		return clienteRepository.findById(id).orElse(null);
	}

	@Override
	public Cliente actualizar(Cliente entidad) {
		return clienteRepository.save(entidad);
	}

	@Override
	public void eliminar(Long id) {
		clienteRepository.deleteById(id);
	}

}
