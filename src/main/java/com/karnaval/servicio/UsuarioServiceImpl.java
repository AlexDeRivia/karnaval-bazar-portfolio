package com.karnaval.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.karnaval.entidad.Usuario;
import com.karnaval.repositorio.UsuarioRepository;
@Service
public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public Usuario agregar(Usuario entidad) {
		entidad.setClave(passwordEncoder.encode(entidad.getClave()));
		return usuarioRepository.save(entidad);
	}

	@Override
	public List<Usuario> listarTodos() {
		return usuarioRepository.findAll();
	}

	@Override
	public Usuario buscar(Long id) {
		return usuarioRepository.findById(id).orElse(null);
	}

	@Override
	public Usuario actualizar(Usuario entidad) {
		return usuarioRepository.save(entidad);
	}

	@Override
	public void eliminar(Long id) {
		usuarioRepository.deleteById(id);
	}
	
	public Usuario buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre);
    }
}
