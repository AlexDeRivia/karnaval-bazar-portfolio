const result = document.querySelector("#order-result");
const title = document.querySelector("#order-title");
const message = document.querySelector("#order-message");
const invoice = document.querySelector("#invoice-link");

function render(status) {
    if (status === "PAID") {
        title.textContent = "Compra confirmada";
        message.textContent = "Tu pedido fue confirmado. Puedes descargar tu comprobante.";
        invoice.classList.remove("d-none");
        localStorage.removeItem("productos-en-carrito");
        return true;
    }
    if (status === "FAILED" || status === "EXPIRED") {
        title.textContent = "No se confirmó el pago";
        message.textContent = "Puedes regresar al carrito e intentarlo de nuevo.";
        return true;
    }
    return false;
}

if (!render(result.dataset.orderStatus)) {
    let attempts = 0;
    const timer = setInterval(async () => {
        attempts++;
        try {
            const response = await fetch(`/checkout/status/${encodeURIComponent(result.dataset.orderId)}`, {
                cache: "no-store"
            });
            if (response.ok && render((await response.json()).status)) {
                clearInterval(timer);
            }
        } catch {
            // The next poll can recover from a temporary connection failure.
        }
        if (attempts >= 45) {
            clearInterval(timer);
            message.textContent = "La confirmación aún está pendiente. Recarga esta página en unos minutos.";
        }
    }, 2000);
}
