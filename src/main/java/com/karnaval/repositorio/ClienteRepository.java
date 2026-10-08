package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karnaval.entidad.Cliente;

public interface ClienteRepository
		extends JpaRepository<Cliente, Long> {

}
