package com.karnaval.controlador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.karnaval.entidad.Producto;
import com.karnaval.repositorio.ProductoRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;

@Controller
public class CheckoutController {
    private final ProductoRepository productos;
    private final String mode;
    private final String stripeSecret;
    private final String publicBaseUrl;

    public CheckoutController(ProductoRepository productos,
            @Value("${app.checkout.mode:disabled}") String mode,
            @Value("${app.stripe.secret-key:}") String stripeSecret,
            @Value("${app.public-base-url:}") String publicBaseUrl) {
        this.productos = productos;
        this.mode = mode;
        this.stripeSecret = stripeSecret;
        this.publicBaseUrl = publicBaseUrl;
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(name = "item", required = false) List<String> entries,
            Model model) {
        if ("disabled".equals(mode)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Checkout desactivado");
        }
        List<CartLine> lines = validateCart(entries);
        BigDecimal total = lines.stream().map(line -> line.producto().getPrecio()
                .multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if ("demo".equals(mode)) {
            model.addAttribute("lines", lines);
            model.addAttribute("total", total);
            return "checkout/demo";
        }
        if (!"stripe-test".equals(mode) || !stripeSecret.startsWith("sk_test_")
                || !publicBaseUrl.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Checkout no configurado");
        }
        String baseUrl = publicBaseUrl.replaceAll("/+$", "");
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/checkout/result?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/message-responses/openCancel");
        for (CartLine line : lines) {
            Producto product = line.producto();
            params.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) line.quantity())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("pen")
                            .setUnitAmount(product.getPrecio().movePointRight(2).longValueExact())
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(product.getNombre()).build())
                            .build())
                    .build());
        }
        try {
            Session session = Session.create(params.build(), stripeOptions());
            return "redirect:" + session.getUrl();
        } catch (StripeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "El checkout de prueba no está disponible");
        }
    }

    @GetMapping("/checkout/result")
    public String result(@RequestParam(name = "session_id", required = false) String sessionId,
            Model model) {
        boolean paid = false;
        if ("stripe-test".equals(mode) && stripeSecret.startsWith("sk_test_")
                && sessionId != null && sessionId.matches("cs_test_[A-Za-z0-9]{10,200}")) {
            try {
                Session session = Session.retrieve(sessionId, stripeOptions());
                paid = Boolean.FALSE.equals(session.getLivemode())
                        && "paid".equals(session.getPaymentStatus());
            } catch (StripeException ignored) {
                // A failed verification must never produce a success message.
            }
        }
        model.addAttribute("paid", paid);
        return "checkout/result";
    }

    private RequestOptions stripeOptions() {
        return RequestOptions.builder().setApiKey(stripeSecret).build();
    }

    private List<CartLine> validateCart(List<String> entries) {
        if (entries == null || entries.isEmpty() || entries.size() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrito inválido");
        }
        List<CartLine> lines = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (String entry : entries) {
            if (!entry.matches("[1-9][0-9]{0,9}:[1-9][0-9]{0,1}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inválido");
            }
            String[] parts = entry.split(":");
            long id = Long.parseLong(parts[0]);
            int quantity = Integer.parseInt(parts[1]);
            if (!seen.add(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto duplicado");
            }
            Producto product = productos.findById(id).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no disponible"));
            if (quantity > product.getStock() || product.getPrecio() == null
                    || product.getPrecio().signum() < 0 || product.getPrecio().scale() > 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no disponible");
            }
            lines.add(new CartLine(product, quantity));
        }
        return lines;
    }

    public record CartLine(Producto producto, int quantity) {}
}
