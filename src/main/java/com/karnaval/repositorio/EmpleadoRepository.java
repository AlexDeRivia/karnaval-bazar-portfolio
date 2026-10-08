package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karnaval.entidad.Empleado;

public interface EmpleadoRepository
		extends JpaRepository<Empleado, Long> {

}
