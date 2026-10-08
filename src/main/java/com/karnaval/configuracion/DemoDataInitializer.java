package com.karnaval.configuracion;

import java.io.IOException;
import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karnaval.entidad.Cliente;
import com.karnaval.entidad.Empleado;
import com.karnaval.entidad.Genero;
import com.karnaval.entidad.Producto;
import com.karnaval.entidad.Proveedor;
import com.karnaval.repositorio.ClienteRepository;
import com.karnaval.repositorio.EmpleadoRepository;
import com.karnaval.repositorio.ProductoRepository;
import com.karnaval.repositorio.ProveedorRepository;

@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final ProductoRepository productos;
    private final ClienteRepository clientes;
    private final EmpleadoRepository empleados;
    private final ProveedorRepository proveedores;

    public DemoDataInitializer(ObjectMapper objectMapper, ProductoRepository productos,
            ClienteRepository clientes, EmpleadoRepository empleados, ProveedorRepository proveedores) {
        this.objectMapper = objectMapper;
        this.productos = productos;
        this.clientes = clientes;
        this.empleados = empleados;
        this.proveedores = proveedores;
    }

    @Override
    public void run(String... args) throws IOException {
        if (productos.count() == 0) {
            JsonNode catalogo = objectMapper.readTree(new ClassPathResource("static/js/productos.json").getInputStream());
            for (JsonNode item : catalogo) {
                productos.save(new Producto(
                        item.path("titulo").asText(),
                        BigDecimal.valueOf(item.path("precio").asLong()),
                        20,
                        "Artículo de demostración",
                        "Almacén principal",
                        item.path("imagen").asText().replaceFirst("^\\.", ""),
                        item.path("categoria").path("id").asText()));
            }
        }
        if (clientes.count() == 0) {
            clientes.save(new Cliente("12345678", "Ejemplo", "Demo", "Cliente", null,
                    "900000001", "cliente@example.com", "Dirección de ejemplo", Genero.FEMENINO));
        }
        if (empleados.count() == 0) {
            empleados.save(new Empleado("87654321", "Ejemplo", "Demo", "Empleado", null,
                    "900000002", "empleado@example.com", "Dirección de ejemplo"));
        }
        if (proveedores.count() == 0) {
            proveedores.save(new Proveedor("20123456789", "Proveedor de ejemplo", "900000003", "Dirección de ejemplo"));
        }
    }
}
