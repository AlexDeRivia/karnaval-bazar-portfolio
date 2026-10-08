package com.karnaval.entidad;

import java.io.Serializable;

import lombok.Data;

@Data
public class CompraDetalleID implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long compra;
	private Long producto;

	public CompraDetalleID() {
		super();
	}

	public CompraDetalleID(Long compra, Long producto) {
		super();
		this.compra = compra;
		this.producto = producto;
	}
}
