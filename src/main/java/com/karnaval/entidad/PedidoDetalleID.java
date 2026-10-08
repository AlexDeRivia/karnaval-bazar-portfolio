package com.karnaval.entidad;

import java.io.Serializable;

import lombok.Data;

@Data
public class PedidoDetalleID implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long pedido;
	private Long producto;

	public PedidoDetalleID() {
		super();
	}

	public PedidoDetalleID(Long pedido, Long producto) {
		super();
		this.pedido = pedido;
		this.producto = producto;
	}
}
