package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karnaval.entidad.Proveedor;
import com.karnaval.repositorio.ProveedorRepository;
@Service
public class ProveedorServiceImpl implements ProveedorService {

	@Autowired
	private ProveedorRepository proveedorRepository;

	@Override
	public Proveedor agregar(Proveedor entidad) {
		return proveedorRepository.save(entidad);
	}

	@Override
	public List<Proveedor> listarTodos() {
		return proveedorRepository.findAll();
	}

	@Override
	public Proveedor buscar(Long id) {
		return proveedorRepository.findById(id).orElse(null);
	}

	@Override
	public Proveedor actualizar(Proveedor entidad) {
		return proveedorRepository.save(entidad);
	}

	@Override
	public void eliminar(Long id) {
		proveedorRepository.deleteById(id);
	}

}
