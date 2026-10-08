package com.karnaval;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import com.karnaval.repositorio.CompraRepository;
import com.karnaval.repositorio.EmpleadoRepository;
import com.karnaval.repositorio.ProductoRepository;
import com.karnaval.repositorio.ProveedorRepository;

@SpringBootTest
@AutoConfigureMockMvc
class KarnavalBazaarApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ProductoRepository productos;
    @Autowired ProveedorRepository proveedores;
    @Autowired EmpleadoRepository empleados;
    @Autowired CompraRepository compras;

    @Test
    void publicCatalogAndCartRender() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Catálogo de prueba")));
        mvc.perform(get("/shoopingCar/openCar"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Checkout no disponible")));
        mvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(50)))
                .andExpect(jsonPath("$[0].precio").isNumber());
    }

    @Test
    void checkoutRequiresConfigurationAndCsrf() throws Exception {
        String item = productos.findAll().get(0).getId() + ":2";
        mvc.perform(post("/checkout").param("item", item))
                .andExpect(status().isForbidden());
        mvc.perform(post("/checkout").with(csrf()).param("item", item))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void backOfficeRequiresAuthenticationAndLoginRenders() throws Exception {
        mvc.perform(get("/producto/index"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("Usuario/paginaLogin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void backOfficeScreensRenderWithDemoData() throws Exception {
        for (String path : new String[] {
                "/admin/index", "/producto/index", "/producto/nuevo",
                "/cliente/index", "/cliente/nuevo", "/proveedor/index",
                "/proveedor/nuevo", "/empleado/index", "/empleado/nuevo",
                "/compra/index", "/compra/nuevo" }) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    @Transactional
    @WithMockUser(roles = "ADMIN")
    void purchaseCanBeSavedAndViewed() throws Exception {
        String providerId = proveedores.findAll().get(0).getId().toString();
        String employeeId = empleados.findAll().get(0).getId().toString();
        String productId = productos.findAll().get(0).getId().toString();
        mvc.perform(post("/compra/guardar").with(csrf())
                .param("proveedor.id", providerId)
                .param("empleado.id", employeeId)
                .param("fecha", "2026-10-07")
                .param("compraDetalles[0].producto.id", productId)
                .param("compraDetalles[0].cantidad", "2")
                .param("compraDetalles[0].precioUnitario", "10.50"))
                .andExpect(status().is3xxRedirection());
        Long id = compras.findAll().get(0).getId();
        mvc.perform(get("/compra/ver/" + id)).andExpect(status().isOk());
    }
}
