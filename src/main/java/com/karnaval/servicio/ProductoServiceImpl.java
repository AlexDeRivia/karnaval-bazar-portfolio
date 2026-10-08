package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karnaval.entidad.Producto;
import com.karnaval.repositorio.ProductoRepository;
@Service
public class ProductoServiceImpl implements ProductoService {

	@Autowired
	private ProductoRepository productoRepository;

	@Override
	public Producto agregar(Producto entidad) {
		return productoRepository.save(entidad);
	}

	@Override
	public List<Producto> listarTodos() {
		return productoRepository.findAll();
	}

	@Override
	public Producto buscar(Long id) {
		return productoRepository.findById(id).orElse(null);
	}

	@Override
	public Producto actualizar(Producto entidad) {
		return productoRepository.save(entidad);
	}

	@Override
	public void eliminar(Long id) {
		productoRepository.deleteById(id);
	}

}
