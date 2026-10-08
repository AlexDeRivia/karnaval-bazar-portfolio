package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karnaval.entidad.Compra;
import com.karnaval.repositorio.CompraRepository;

@Service
public class CompraServiceImpl implements CompraService {

	@Autowired
    private CompraRepository compraRepository;

    @Override
    public Compra agregar(Compra entidad) {
        return compraRepository.save(entidad);
    }

    @Override
    public List<Compra> listarTodos() {
        return compraRepository.findAll();
    }

    @Override
    public Compra buscar(Long id) {
        return compraRepository.findById(id).orElse(null);
    }

    @Override
    public Compra actualizar(Compra entidad) {
        return compraRepository.save(entidad);
    }

    @Override
    public void eliminar(Long id) {
        compraRepository.deleteById(id);
    }
}
