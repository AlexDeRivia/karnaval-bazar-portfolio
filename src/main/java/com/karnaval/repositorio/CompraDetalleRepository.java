package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karnaval.entidad.CompraDetalle;
import com.karnaval.entidad.CompraDetalleID;

public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, CompraDetalleID> {

}
