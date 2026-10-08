package com.karnaval.servicio;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.karnaval.entidad.OnlineOrder;
import com.karnaval.entidad.OnlineOrderLine;
import com.karnaval.entidad.OnlineOrderStatus;
import com.karnaval.repositorio.OnlineOrderRepository;
import com.karnaval.repositorio.ProductoRepository;

@Service
public class OnlineOrderService {
    private final OnlineOrderRepository orders;
    private final ProductoRepository products;

    public OnlineOrderService(OnlineOrderRepository orders, ProductoRepository products) {
        this.orders = orders;
        this.products = products;
    }

    @Transactional
    public OnlineOrder create(List<OnlineOrderLine> lines) {
        if (lines == null || lines.isEmpty() || lines.stream().anyMatch(line -> line.getProductId() == null
                || line.getQuantity() < 1 || line.getUnitAmount() < 1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrito inválido");
        }
        // Conditional updates serialize competing checkouts at the database level.
        for (OnlineOrderLine line : lines.stream()
                .sorted(java.util.Comparator.comparing(OnlineOrderLine::getProductId)).toList()) {
            if (products.reserve(line.getProductId(), line.getQuantity(),
                    BigDecimal.valueOf(line.getUnitAmount(), 2)) != 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El precio o stock cambió. Actualiza tu carrito.");
            }
        }
        return orders.save(new OnlineOrder(lines));
    }

    @Transactional
    public void attachSession(String id, String sessionId) {
        OnlineOrder order = required(id);
        if (order.getStatus() != OnlineOrderStatus.PENDING || order.getStripeSessionId() != null) {
            throw new IllegalStateException("El pedido ya tiene una sesión de pago");
        }
        order.setStripeSessionId(sessionId);
    }

    @Transactional
    public void failCreation(String id) {
        OnlineOrder order = required(id);
        if (order.getStatus() == OnlineOrderStatus.PENDING && order.getStripeSessionId() == null) {
            order.setStatus(OnlineOrderStatus.FAILED);
            releaseStock(order);
        }
    }

    @Transactional
    public void expireMissingSession(String id) {
        OnlineOrder order = required(id);
        if (order.getStatus() == OnlineOrderStatus.PENDING && order.getStripeSessionId() == null) {
            order.setStatus(OnlineOrderStatus.EXPIRED);
            releaseStock(order);
        }
    }

    @Transactional(readOnly = true)
    public OnlineOrder required(String id) {
        if (id == null || !id.matches("[0-9a-f-]{36}")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void applyStripeSession(String type, JsonNode session) {
        String id = session.path("client_reference_id").asText("");
        OnlineOrder order = required(id);
        String sessionId = session.path("id").asText("");
        if (!sessionId.equals(order.getStripeSessionId()) || !sessionId.startsWith("cs_test_")
                || session.path("livemode").asBoolean(true)
                || !"payment".equals(session.path("mode").asText())
                || !"pen".equals(session.path("currency").asText())
                || session.path("amount_total").asLong(-1) != order.getTotalAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sesión no coincide con el pedido");
        }
        if (order.getStatus() == OnlineOrderStatus.PAID) {
            return;
        }
        if (order.getStatus() != OnlineOrderStatus.PENDING) {
            return;
        }
        if (("checkout.session.completed".equals(type)
                || "checkout.session.async_payment_succeeded".equals(type))
                && "paid".equals(session.path("payment_status").asText())) {
            order.setStatus(OnlineOrderStatus.PAID);
            order.setPaidAt(Instant.now());
            String email = session.path("customer_details").path("email").asText("");
            if (!email.isBlank()) {
                order.setCustomerEmail(email.length() > 255 ? email.substring(0, 255) : email);
            }
        } else if ("checkout.session.async_payment_failed".equals(type)) {
            order.setStatus(OnlineOrderStatus.FAILED);
            releaseStock(order);
        } else if ("checkout.session.expired".equals(type)) {
            order.setStatus(OnlineOrderStatus.EXPIRED);
            releaseStock(order);
        }
    }

    private void releaseStock(OnlineOrder order) {
        if (!order.isStockReserved()) {
            return;
        }
        for (OnlineOrderLine line : order.getLines()) {
            if (products.release(line.getProductId(), line.getQuantity()) != 1) {
                throw new IllegalStateException("No se pudo liberar el stock del pedido " + order.getId());
            }
        }
        order.setStockReserved(false);
    }
}
