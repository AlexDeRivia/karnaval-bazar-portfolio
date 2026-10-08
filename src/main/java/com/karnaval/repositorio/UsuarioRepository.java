package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.karnaval.entidad.Usuario;
@Repository
public interface UsuarioRepository
		extends JpaRepository<Usuario, Long> {

	Usuario findByNombre(String nombre);
	
}
