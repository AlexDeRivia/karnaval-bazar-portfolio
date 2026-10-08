package com.karnaval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.karnaval.entidad.OnlineOrder;
import com.karnaval.entidad.OnlineOrderStatus;
import com.karnaval.repositorio.OnlineOrderRepository;
import com.karnaval.repositorio.ProductoRepository;
import com.karnaval.servicio.StripeCheckoutGateway;
import com.stripe.model.checkout.Session;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:stripe-flow;DB_CLOSE_DELAY=-1",
        "app.checkout.mode=stripe-test",
        "app.stripe.secret-key=sk_test_local_only",
        "app.stripe.publishable-key=pk_test_local_only",
        "app.stripe.webhook-secret=whsec_local_only",
        "app.public-base-url=http://localhost:8080"
})
@AutoConfigureMockMvc
class StripeCheckoutFlowTests {
    @Autowired MockMvc mvc;
    @Autowired ProductoRepository products;
    @Autowired OnlineOrderRepository orders;
    @MockitoBean StripeCheckoutGateway gateway;

    @Test
    void signedWebhookConfirmsOrderAndUnlocksPdf() throws Exception {
        Session session = new Session();
        session.setId("cs_test_12345678901234567890");
        session.setLivemode(false);
        session.setUrl("https://checkout.stripe.com/c/pay/test-session");
        when(gateway.create(any())).thenReturn(session);

        var product = products.findAll().get(0);
        int initialStock = product.getStock();
        mvc.perform(get("/shoopingCar/openCar"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("checkout-form")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No se realizará ningún cargo real.")));
        mvc.perform(post("/checkout").param("item", product.getId() + ":2"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/checkout").with(csrf()).param("item", "999999:1"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/checkout").with(csrf()).param("item", product.getId() + ":2"))
                .andExpect(status().is3xxRedirection());

        OnlineOrder order = orders.findAll().stream()
                .filter(candidate -> session.getId().equals(candidate.getStripeSessionId()))
                .findFirst().orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OnlineOrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualTo(product.getPrecio().movePointRight(2).longValueExact() * 2);
        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(initialStock - 2);
        mvc.perform(get("/admin/pedidos/" + order.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(product.getNombre())));
        mvc.perform(get("/checkout/result").param("order", order.getId())
                        .param("session_id", "cs_test_wrong"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/checkout/result").param("order", order.getId())
                        .param("session_id", session.getId()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Referrer-Policy", "no-referrer"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Estamos confirmando")));
        mvc.perform(get("/checkout/invoice/" + order.getId()).param("session_id", session.getId()))
                .andExpect(status().isConflict());
        mvc.perform(get("/checkout/invoice/" + order.getId()).param("session_id", "cs_test_wrong"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/stripe/webhook").contentType(MediaType.APPLICATION_JSON)
                        .content(new byte[65_537]))
                .andExpect(status().isPayloadTooLarge());

        String event = event(order, order.getTotalAmount());
        mvc.perform(post("/stripe/webhook").contentType(MediaType.APPLICATION_JSON)
                        .content(event).header("Stripe-Signature", "t=0,v1=invalid"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/stripe/webhook").contentType(MediaType.APPLICATION_JSON)
                        .content(event(order, order.getTotalAmount() + 100))
                        .header("Stripe-Signature", sign(event(order, order.getTotalAmount() + 100))))
                .andExpect(status().isBadRequest());
        assertThat(orders.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OnlineOrderStatus.PENDING);

        mvc.perform(post("/stripe/webhook").contentType(MediaType.APPLICATION_JSON)
                        .content(event).header("Stripe-Signature", sign(event)))
                .andExpect(status().isOk());
        mvc.perform(post("/stripe/webhook").contentType(MediaType.APPLICATION_JSON)
                        .content(event).header("Stripe-Signature", sign(event)))
                .andExpect(status().isOk());
        assertThat(orders.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OnlineOrderStatus.PAID);
        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(initialStock - 2);
        mvc.perform(get("/checkout/status/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"PAID\"}"));
        mvc.perform(get("/checkout/result").param("order", order.getId())
                        .param("session_id", session.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Compra confirmada")));

        byte[] pdf = mvc.perform(get("/checkout/invoice/" + order.getId())
                        .param("session_id", session.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        String samplePath = System.getProperty("invoice.sample.path");
        if (samplePath != null) {
            Files.write(Path.of(samplePath), pdf);
        }
        try (var document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("BAZAR CENTRAL", "FACTURA INFORMATIVA", product.getNombre());
        }
    }

    @Test
    void expiredSessionReturnsReservedStock() throws Exception {
        Session session = new Session();
        session.setId("cs_test_expiration_flow_123456");
        session.setLivemode(false);
        session.setUrl("https://checkout.stripe.com/c/pay/expired-session");
        when(gateway.create(any())).thenReturn(session);
        var product = products.findAll().get(1);
        int initialStock = product.getStock();

        mvc.perform(post("/checkout").with(csrf()).param("item", product.getId() + ":1"))
                .andExpect(status().is3xxRedirection());
        OnlineOrder order = orders.findAll().stream()
                .filter(candidate -> session.getId().equals(candidate.getStripeSessionId()))
                .findFirst().orElseThrow();
        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(initialStock - 1);

        String expired = event(order, order.getTotalAmount())
                .replace("checkout.session.completed", "checkout.session.expired")
                .replace("\"payment_status\":\"paid\"", "\"payment_status\":\"unpaid\"");
        mvc.perform(post("/stripe/webhook").contentType(MediaType.APPLICATION_JSON)
                        .content(expired).header("Stripe-Signature", sign(expired)))
                .andExpect(status().isOk());
        mvc.perform(post("/stripe/webhook").contentType(MediaType.APPLICATION_JSON)
                        .content(expired).header("Stripe-Signature", sign(expired)))
                .andExpect(status().isOk());
        assertThat(orders.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OnlineOrderStatus.EXPIRED);
        assertThat(products.findById(product.getId()).orElseThrow().getStock()).isEqualTo(initialStock);
    }

    private static String event(OnlineOrder order, long amount) {
        return "{\"id\":\"evt_test_checkout\",\"object\":\"event\",\"api_version\":\"2024-06-20\","
                + "\"type\":\"checkout.session.completed\",\"data\":{\"object\":{"
                + "\"id\":\"" + order.getStripeSessionId() + "\",\"object\":\"checkout.session\","
                + "\"client_reference_id\":\"" + order.getId() + "\",\"livemode\":false,"
                + "\"mode\":\"payment\",\"currency\":\"pen\",\"amount_total\":" + amount + ","
                + "\"payment_status\":\"paid\",\"customer_details\":{\"email\":\"cliente@example.com\"}}}}";
    }

    private static String sign(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("whsec_local_only".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
        return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(hash);
    }
}
