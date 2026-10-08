package com.karnaval.controlador;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.karnaval.configuracion.StripeSettings;
import com.karnaval.entidad.OnlineOrder;
import com.karnaval.entidad.OnlineOrderLine;
import com.karnaval.entidad.OnlineOrderStatus;
import com.karnaval.entidad.Producto;
import com.karnaval.repositorio.ProductoRepository;
import com.karnaval.servicio.InvoicePdfService;
import com.karnaval.servicio.OnlineOrderService;
import com.karnaval.servicio.StripeCheckoutGateway;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

@Controller
public class CheckoutController {
    private final ProductoRepository products;
    private final OnlineOrderService orders;
    private final StripeCheckoutGateway gateway;
    private final StripeSettings settings;
    private final InvoicePdfService invoices;

    public CheckoutController(ProductoRepository products, OnlineOrderService orders,
            StripeCheckoutGateway gateway, StripeSettings settings, InvoicePdfService invoices) {
        this.products = products;
        this.orders = orders;
        this.gateway = gateway;
        this.settings = settings;
        this.invoices = invoices;
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(name = "item", required = false) List<String> entries) {
        if (!settings.ready()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Checkout no configurado");
        }
        OnlineOrder order;
        try {
            order = orders.create(validateCart(entries));
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Importe inválido");
        }
        try {
            Session session = gateway.create(order);
            if (session.getId() == null || !session.getId().startsWith("cs_test_")
                    || !Boolean.FALSE.equals(session.getLivemode()) || session.getUrl() == null
                    || !session.getUrl().startsWith("https://checkout.stripe.com/")) {
                orders.failCreation(order.getId());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Sesión de pago inválida");
            }
            orders.attachSession(order.getId(), session.getId());
            return "redirect:" + session.getUrl();
        } catch (StripeException ex) {
            orders.failCreation(order.getId());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe Checkout no está disponible");
        }
    }

    @GetMapping("/checkout/result")
    public String result(@RequestParam String order, @RequestParam(name = "session_id") String sessionId,
            Model model) {
        OnlineOrder current = orders.required(order);
        if (!sessionId.equals(current.getStripeSessionId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("order", current);
        return "checkout/result";
    }

    @GetMapping("/checkout/status/{id}")
    public ResponseEntity<OrderStatus> status(@PathVariable String id) {
        OnlineOrder order = orders.required(id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new OrderStatus(order.getStatus().name()));
    }

    @GetMapping("/checkout/invoice/{id}")
    public ResponseEntity<byte[]> invoice(@PathVariable String id) {
        OnlineOrder order = orders.required(id);
        if (order.getStatus() != OnlineOrderStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pedido aún no confirmado");
        }
        byte[] pdf = invoices.generate(order);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=factura-muestra-" + order.getId() + ".pdf")
                .body(pdf);
    }

    @GetMapping("/checkout/cancel")
    public String cancel(@RequestParam String order, Model model) {
        model.addAttribute("order", orders.required(order));
        return "checkout/cancel";
    }

    private List<OnlineOrderLine> validateCart(List<String> entries) {
        if (entries == null || entries.isEmpty() || entries.size() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrito inválido");
        }
        List<OnlineOrderLine> lines = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (String entry : entries) {
            if (entry == null || !entry.matches("[1-9][0-9]{0,9}:[1-9][0-9]{0,1}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto inválido");
            }
            String[] parts = entry.split(":");
            long id = Long.parseLong(parts[0]);
            int quantity = Integer.parseInt(parts[1]);
            if (!seen.add(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto duplicado");
            }
            Producto product = products.findById(id).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no disponible"));
            if (product.getStock() == null || quantity > product.getStock()
                    || product.getPrecio() == null || product.getPrecio().signum() <= 0
                    || product.getPrecio().scale() > 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no disponible");
            }
            long unitAmount = product.getPrecio().movePointRight(2).longValueExact();
            lines.add(new OnlineOrderLine(product.getId(), product.getNombre(), quantity, unitAmount));
        }
        return lines;
    }

    public record OrderStatus(String status) {}
}
