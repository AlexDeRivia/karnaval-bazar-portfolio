package com.karnaval;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:karnaval-auth;DB_CLOSE_DELAY=-1",
        "app.admin.username=admin-prueba",
        "app.admin.password=clave-admin-prueba-123"
})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class AdminAuthenticationTests {
    @Autowired MockMvc mvc;

    @Test
    void configuredAdminCanSignInAndOpenPanel() throws Exception {
        mvc.perform(get("/admin/pedidos"))
                .andExpect(status().is3xxRedirection());
        var login = mvc.perform(formLogin("/login")
                        .user("admin-prueba").password("clave-admin-prueba-123"))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        for (String path : new String[] {
                "/admin/index", "/admin/pedidos", "/producto/index", "/producto/nuevo",
                "/cliente/index", "/cliente/nuevo", "/proveedor/index", "/proveedor/nuevo",
                "/empleado/index", "/empleado/nuevo", "/compra/index", "/compra/nuevo"
        }) {
            mvc.perform(get(path).session(session))
                    .andExpect(status().isOk());
        }
    }
}
