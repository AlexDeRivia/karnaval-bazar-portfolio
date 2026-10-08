package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karnaval.entidad.PedidoDetalle;
import com.karnaval.entidad.PedidoDetalleID;

public interface PedidoDetalleRepository 
		extends JpaRepository<PedidoDetalle, PedidoDetalleID> {

}
