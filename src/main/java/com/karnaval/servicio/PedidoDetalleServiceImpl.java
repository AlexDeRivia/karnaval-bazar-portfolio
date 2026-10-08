package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.karnaval.entidad.PedidoDetalle;
import com.karnaval.entidad.PedidoDetalleID;
import com.karnaval.repositorio.PedidoDetalleRepository;

public class PedidoDetalleServiceImpl implements PedidoDetalleService {

	@Autowired
	private PedidoDetalleRepository pedidoDetalleRepository;

	@Override
	public PedidoDetalle agregar(PedidoDetalle entidad) {
		return pedidoDetalleRepository.save(entidad);
	}

	@Override
	public List<PedidoDetalle> listarTodos() {
		return pedidoDetalleRepository.findAll();
	}

	@Override
	public PedidoDetalle buscar(PedidoDetalleID id) {
		return pedidoDetalleRepository.findById(id).orElse(null);
	}

	@Override
	public PedidoDetalle actualizar(PedidoDetalle entidad) {
		return pedidoDetalleRepository.save(entidad);
	}

	@Override
	public void eliminar(PedidoDetalleID id) {
		pedidoDetalleRepository.deleteById(id);
	}

}
