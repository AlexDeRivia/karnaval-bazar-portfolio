package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

import com.karnaval.entidad.Compra;


public interface CompraRepository extends JpaRepository<Compra, Long> {
	@EntityGraph(attributePaths = {"proveedor", "empleado"})
	List<Compra> findAll();

	@EntityGraph(attributePaths = {"proveedor", "empleado", "compraDetalles", "compraDetalles.producto"})
	Optional<Compra> findById(Long id);
}
