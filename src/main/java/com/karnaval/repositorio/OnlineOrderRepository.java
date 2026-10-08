package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karnaval.entidad.OnlineOrder;

public interface OnlineOrderRepository extends JpaRepository<OnlineOrder, String> {
}
