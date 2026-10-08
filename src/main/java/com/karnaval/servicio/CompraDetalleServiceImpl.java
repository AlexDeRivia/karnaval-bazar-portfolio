package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karnaval.entidad.CompraDetalle;
import com.karnaval.entidad.CompraDetalleID;
import com.karnaval.repositorio.CompraDetalleRepository;

@Service
public class CompraDetalleServiceImpl implements CompraDetalleService {

	@Autowired
    private CompraDetalleRepository compraDetalleRepository;

    @Override
    public CompraDetalle agregar(CompraDetalle entidad) {
        return compraDetalleRepository.save(entidad);
    }

    @Override
    public List<CompraDetalle> listarTodos() {
        return compraDetalleRepository.findAll();
    }

    @Override
    public CompraDetalle buscar(CompraDetalleID id) {
        return compraDetalleRepository.findById(id).orElse(null);
    }

    @Override
    public CompraDetalle actualizar(CompraDetalle entidad) {
        return compraDetalleRepository.save(entidad);
    }

    @Override
    public void eliminar(CompraDetalleID id) {
        compraDetalleRepository.deleteById(id);
    }
}