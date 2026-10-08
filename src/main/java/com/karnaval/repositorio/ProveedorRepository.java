package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karnaval.entidad.Proveedor;

public interface ProveedorRepository
		extends JpaRepository<Proveedor, Long> {

}
