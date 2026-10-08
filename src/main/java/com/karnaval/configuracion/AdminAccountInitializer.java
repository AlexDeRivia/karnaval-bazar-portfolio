package com.karnaval.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.karnaval.entidad.Usuario;
import com.karnaval.servicio.UsuarioServiceImpl;

@Component
public class AdminAccountInitializer implements CommandLineRunner {
    private final UsuarioServiceImpl usuarios;
    private final String username;
    private final String password;
    private final PasswordEncoder encoder;

    public AdminAccountInitializer(UsuarioServiceImpl usuarios, PasswordEncoder encoder,
            @Value("${app.admin.username:}") String username,
            @Value("${app.admin.password:}") String password) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (username.isBlank() && password.isBlank()) {
            disableOtherAdministrators(null);
            return;
        }
        if (username.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Configure ADMIN_USERNAME and an ADMIN_PASSWORD of at least 12 characters");
        }
        Usuario current = usuarios.buscarPorNombre(username);
        if (current == null) {
            usuarios.agregar(new Usuario(username, password, "ADMIN", 1));
        } else {
            if (!encoder.matches(password, current.getClave())) {
                current.setClave(encoder.encode(password));
            }
            current.setRol("ADMIN");
            current.setEstado(1);
            usuarios.actualizar(current);
        }
        disableOtherAdministrators(username);
    }

    private void disableOtherAdministrators(String activeUsername) {
        for (Usuario usuario : usuarios.listarTodos()) {
            if ("ADMIN".equals(usuario.getRol()) && !usuario.getNombre().equals(activeUsername)
                    && usuario.getEstado() != 0) {
                usuario.setEstado(0);
                usuarios.actualizar(usuario);
            }
        }
    }
}
