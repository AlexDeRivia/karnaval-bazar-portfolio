package com.karnaval.configuracion;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeSettings {
    private final String mode;
    private final String secretKey;
    private final String publishableKey;
    private final String webhookSecret;
    private final String baseUrl;

    public StripeSettings(@Value("${app.checkout.mode:disabled}") String mode,
            @Value("${app.stripe.secret-key:}") String secretKey,
            @Value("${app.stripe.publishable-key:}") String publishableKey,
            @Value("${app.stripe.webhook-secret:}") String webhookSecret,
            @Value("${app.public-base-url:}") String baseUrl) {
        this.mode = mode;
        this.secretKey = secretKey;
        this.publishableKey = publishableKey;
        this.webhookSecret = webhookSecret;
        this.baseUrl = baseUrl;
    }

    public boolean ready() {
        if (!"stripe-test".equals(mode) || !secretKey.startsWith("sk_test_")
                || !publishableKey.startsWith("pk_test_") || !webhookSecret.startsWith("whsec_")) {
            return false;
        }
        try {
            URI uri = URI.create(baseUrl);
            boolean local = "http".equals(uri.getScheme())
                    && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost()));
            return ("https".equals(uri.getScheme()) || local) && uri.getHost() != null
                    && (uri.getPath().isEmpty() || "/".equals(uri.getPath()))
                    && uri.getUserInfo() == null && uri.getQuery() == null && uri.getFragment() == null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String secretKey() { return secretKey; }
    public String webhookSecret() { return webhookSecret; }
    public String baseUrl() { return baseUrl.replaceAll("/+$", ""); }
}
