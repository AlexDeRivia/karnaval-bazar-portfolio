package com.karnaval.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.karnaval.entidad.Usuario;
import com.karnaval.repositorio.UsuarioRepository;

@Configuration
public class SecurityConfig {
    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return username -> {
            Usuario usuario = usuarios.findByNombre(username);
            if (usuario == null) {
                throw new UsernameNotFoundException("Usuario desconocido");
            }
            return new User(usuario.getNombre(), usuario.getClave(), usuario.getEstado() == 1,
                    true, true, true,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())));
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/stripe/webhook"))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.GET, "/gestion").permitAll()
                        .requestMatchers("/", "/index", "/shoopingCar/openCar",
                                "/message-responses/**", "/css/**", "/js/**", "/img/**",
                                "/favicon.ico", "/favicon.svg", "/favicon.png", "/api/catalog", "/checkout/**", "/stripe/webhook",
                                "/admin/login", "/admin/denegado").permitAll()
                        .requestMatchers("/admin/**", "/cliente/**", "/empleado/**",
                                "/producto/**", "/proveedor/**", "/compra/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .formLogin(form -> form.loginPage("/admin/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/admin/index", true)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .build();
    }
}
