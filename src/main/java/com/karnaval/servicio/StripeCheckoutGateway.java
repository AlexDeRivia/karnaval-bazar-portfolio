package com.karnaval.servicio;

import org.springframework.stereotype.Service;

import com.karnaval.configuracion.StripeSettings;
import com.karnaval.entidad.OnlineOrder;
import com.karnaval.entidad.OnlineOrderLine;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class StripeCheckoutGateway {
    private final StripeSettings settings;

    public StripeCheckoutGateway(StripeSettings settings) {
        this.settings = settings;
    }

    public Session create(OnlineOrder order) throws StripeException {
        String baseUrl = settings.baseUrl();
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setClientReferenceId(order.getId())
                .putMetadata("order_id", order.getId())
                .setSuccessUrl(baseUrl + "/checkout/result?order=" + order.getId()
                        + "&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseUrl + "/checkout/cancel?order=" + order.getId());
        for (OnlineOrderLine line : order.getLines()) {
            params.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) line.getQuantity())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("pen")
                            .setUnitAmount(line.getUnitAmount())
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(line.getName()).build())
                            .build())
                    .build());
        }
        RequestOptions options = RequestOptions.builder()
                .setApiKey(settings.secretKey())
                .setIdempotencyKey("online-order-" + order.getId())
                .build();
        return Session.create(params.build(), options);
    }
}
