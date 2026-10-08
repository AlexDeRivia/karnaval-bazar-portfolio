package com.karnaval;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.karnaval.repositorio.ClienteRepository;
import com.karnaval.repositorio.ProductoRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:prod-seed;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.catalog=true"
})
@ActiveProfiles("prod")
class ProdCatalogSeedTests {
    @Autowired ProductoRepository products;
    @Autowired ClienteRepository clients;

    @Test
    void productionProfileLoadsCatalogWithoutSampleCustomers() {
        assertThat(products.count()).isEqualTo(50);
        assertThat(clients.count()).isZero();
    }
}
