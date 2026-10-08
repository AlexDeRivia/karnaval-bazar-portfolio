package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.karnaval.entidad.Producto;
import com.karnaval.entidad.OnlineOrderStatus;
import com.karnaval.repositorio.ProductoRepository;
import com.karnaval.repositorio.OnlineOrderRepository;
@Service
public class ProductoServiceImpl implements ProductoService {

	@Autowired
	private ProductoRepository productoRepository;
	@Autowired
	private OnlineOrderRepository onlineOrders;

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
	@Transactional
	public boolean actualizarSiStockCoincide(Producto producto, int stockEsperado) {
		return productoRepository.updateIfStockMatches(producto.getId(), stockEsperado,
				producto.getNombre(), producto.getPrecio(), producto.getStock(), producto.getDescripcion(),
				producto.getUbicacionAlmacen(), producto.getFoto(), producto.getCategoria()) == 1;
	}

	@Override
	@Transactional
	public void eliminar(Long id) {
		productoRepository.findForUpdate(id).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (onlineOrders.countReservedForProduct(id, OnlineOrderStatus.PENDING) > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"No puedes eliminar un producto con pedidos pendientes");
		}
		productoRepository.deleteById(id);
	}

}
