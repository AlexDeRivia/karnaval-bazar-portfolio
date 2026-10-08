package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karnaval.entidad.Producto;

public interface ProductoRepository
		extends JpaRepository<Producto, Long> {

}
