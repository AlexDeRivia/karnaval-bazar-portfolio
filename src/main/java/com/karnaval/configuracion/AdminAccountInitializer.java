package com.karnaval.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    public void run(String... args) {
        if (username.isBlank() && password.isBlank()) {
            return;
        }
        if (username.isBlank() || password.length() < 12) {
            throw new IllegalStateException("Configure ADMIN_USERNAME and an ADMIN_PASSWORD of at least 12 characters");
        }
        Usuario current = usuarios.buscarPorNombre(username);
        if (current == null) {
            usuarios.agregar(new Usuario(username, password, "ADMIN", 1));
        } else if (!encoder.matches(password, current.getClave())) {
            current.setClave(encoder.encode(password));
            usuarios.actualizar(current);
        }
    }
}
