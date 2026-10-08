package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karnaval.entidad.Empleado;
import com.karnaval.repositorio.EmpleadoRepository;
@Service
public class EmpleadoServiceImpl implements EmpleadoService {

	@Autowired
	private EmpleadoRepository empleadoRepository;

	@Override
	public Empleado agregar(Empleado entidad) {
		return empleadoRepository.save(entidad);
	}

	@Override
	public List<Empleado> listarTodos() {
		return empleadoRepository.findAll();
	}

	@Override
	public Empleado buscar(Long id) {
		return empleadoRepository.findById(id).orElse(null);
	}

	@Override
	public Empleado actualizar(Empleado entidad) {
		return empleadoRepository.save(entidad);
	}

	@Override
	public void eliminar(Long id) {
		empleadoRepository.deleteById(id);
	}

}
