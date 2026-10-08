package com.karnaval.servicio;

import com.karnaval.entidad.Producto;

public interface ProductoService 
		extends iGenericoService<Producto, Long> {
	boolean actualizarSiStockCoincide(Producto producto, int stockEsperado);

}
