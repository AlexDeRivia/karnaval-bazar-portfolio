package com.karnaval.servicio;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.karnaval.configuracion.StripeSettings;
import com.karnaval.entidad.OnlineOrder;
import com.karnaval.entidad.OnlineOrderStatus;
import com.karnaval.repositorio.OnlineOrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

@Component
public class OrderReconciliationJob {
    private static final Logger log = LoggerFactory.getLogger(OrderReconciliationJob.class);
    private final StripeSettings settings;
    private final StripeCheckoutGateway gateway;
    private final OnlineOrderRepository repository;
    private final OnlineOrderService service;
    private final ObjectMapper mapper;

    public OrderReconciliationJob(StripeSettings settings, StripeCheckoutGateway gateway,
            OnlineOrderRepository repository, OnlineOrderService service, ObjectMapper mapper) {
        this.settings = settings;
        this.gateway = gateway;
        this.repository = repository;
        this.service = service;
        this.mapper = mapper;
    }

    @Scheduled(initialDelay = 300_000, fixedDelay = 3_600_000)
    public void reconcile() {
        if (!settings.ready()) {
            return;
        }
        for (OnlineOrder order : repository.findTop100ByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                OnlineOrderStatus.PENDING, Instant.now().minus(Duration.ofHours(2)))) {
            if (order.getStripeSessionId() == null) {
                service.expireMissingSession(order.getId());
                continue;
            }
            try {
                Session session = gateway.retrieve(order.getStripeSessionId());
                String type = null;
                if ("expired".equals(session.getStatus())) {
                    type = "checkout.session.expired";
                } else if ("complete".equals(session.getStatus())
                        && "paid".equals(session.getPaymentStatus())) {
                    type = "checkout.session.completed";
                }
                if (type != null) {
                    service.applyStripeSession(type, sessionNode(session));
                }
            } catch (StripeException | RuntimeException ex) {
                log.warn("No se pudo conciliar el pedido {}: {}", order.getId(), ex.getClass().getSimpleName());
            }
        }
        cleanupTerminalOrders();
    }

    private void cleanupTerminalOrders() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));
        List<OnlineOrder> old = repository.findTop100ByStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
                List.of(OnlineOrderStatus.FAILED, OnlineOrderStatus.EXPIRED), cutoff);
        repository.deleteAll(old);
    }

    private ObjectNode sessionNode(Session session) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", session.getId());
        node.put("client_reference_id", session.getClientReferenceId());
        node.put("livemode", Boolean.TRUE.equals(session.getLivemode()));
        node.put("mode", session.getMode());
        node.put("currency", session.getCurrency());
        node.put("amount_total", session.getAmountTotal() == null ? -1 : session.getAmountTotal());
        node.put("payment_status", session.getPaymentStatus());
        if (session.getCustomerDetails() != null) {
            node.putObject("customer_details").put("email", session.getCustomerDetails().getEmail());
        }
        return node;
    }
}
