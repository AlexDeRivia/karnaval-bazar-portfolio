package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karnaval.entidad.Pedido;

public interface PedidoRepository
		extends JpaRepository<Pedido, Long> {

}
