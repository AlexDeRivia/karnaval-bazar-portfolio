package com.karnaval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import com.karnaval.entidad.OnlineOrderLine;
import com.karnaval.repositorio.ProductoRepository;
import com.karnaval.servicio.OnlineOrderService;
import com.karnaval.servicio.ProductoService;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:stock-reservation;DB_CLOSE_DELAY=-1")
@ActiveProfiles("demo")
class StockReservationTests {
    @Autowired ProductoRepository products;
    @Autowired OnlineOrderService orders;
    @Autowired ProductoService productService;

    @Test
    void reservesAtomicallyAndReleasesOnlyOnce() {
        var product = products.findAll().get(0);
        int initialStock = product.getStock();
        long amount = product.getPrecio().movePointRight(2).longValueExact();
        var order = orders.create(List.of(new OnlineOrderLine(product.getId(),
                product.getNombre(), initialStock, amount)));

        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isZero();
        assertThatThrownBy(() -> orders.create(List.of(new OnlineOrderLine(product.getId(),
                product.getNombre(), 1, amount)))).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> productService.eliminar(product.getId()))
                .isInstanceOf(ResponseStatusException.class);

        orders.failCreation(order.getId());
        orders.failCreation(order.getId());
        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(initialStock);

        var orphan = orders.create(List.of(new OnlineOrderLine(product.getId(),
                product.getNombre(), 1, amount)));
        orders.expireMissingSession(orphan.getId());
        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(initialStock);
    }
}
