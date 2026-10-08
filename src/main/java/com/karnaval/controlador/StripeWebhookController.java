package com.karnaval.controlador;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karnaval.configuracion.StripeSettings;
import com.karnaval.servicio.OnlineOrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

@RestController
public class StripeWebhookController {
    private final StripeSettings settings;
    private final OnlineOrderService orders;
    private final ObjectMapper mapper;

    public StripeWebhookController(StripeSettings settings, OnlineOrderService orders, ObjectMapper mapper) {
        this.settings = settings;
        this.orders = orders;
        this.mapper = mapper;
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<Void> receive(@RequestBody byte[] body,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        if (!settings.ready()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (signature == null || body.length > 65536) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        String payload = new String(body, StandardCharsets.UTF_8);
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, settings.webhookSecret());
        } catch (SignatureVerificationException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Firma de webhook inválida");
        }
        String type = event.getType();
        if ("checkout.session.completed".equals(type)
                || "checkout.session.async_payment_succeeded".equals(type)
                || "checkout.session.async_payment_failed".equals(type)
                || "checkout.session.expired".equals(type)) {
            try {
                JsonNode session = mapper.readTree(payload).path("data").path("object");
                if (!session.isObject()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }
                orders.applyStripeSession(type, session);
            } catch (JsonProcessingException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }
        return ResponseEntity.ok().build();
    }
}
