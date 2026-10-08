const empty = document.querySelector("#carrito-vacio");
const productsContainer = document.querySelector("#carrito-productos");
const actions = document.querySelector("#carrito-acciones");
const total = document.querySelector("#total");
const quantity = document.querySelector("#summary-quantity");
const counter = document.querySelector("#numerito");
const clearButton = document.querySelector("#carrito-acciones-vaciar");
const checkoutForm = document.querySelector("#checkout-form");
const status = document.querySelector("#cart-status");
const notice = document.querySelector("#store-notice");
const template = document.querySelector("#cart-item-template");
const money = new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" });
const categoryNames = {
    tecnologia: "Tecnología",
    "accesorios-mujer": "Accesorios",
    "adornos-hogar": "Hogar",
    vestimenta: "Moda",
    "aseo-limpieza": "Limpieza"
};

function readCart() {
    try {
        const value = JSON.parse(localStorage.getItem("productos-en-carrito"));
        return Array.isArray(value) ? value.filter(item => Number.isInteger(Number(item.id)) && Number.isInteger(item.cantidad) && item.cantidad > 0) : [];
    } catch {
        return [];
    }
}

const cart = readCart();
let products = new Map();
let noticeTimer;

function save() {
    try {
        localStorage.setItem("productos-en-carrito", JSON.stringify(cart));
        return true;
    } catch {
        showNotice("No se pudo guardar el carrito. Inténtalo de nuevo.");
        return false;
    }
}

function showNotice(message) {
    clearTimeout(noticeTimer);
    notice.textContent = message;
    notice.classList.add("visible");
    noticeTimer = setTimeout(() => notice.classList.remove("visible"), 2800);
}

function updateCounter() {
    const count = cart.reduce((sum, item) => sum + item.cantidad, 0);
    counter.textContent = count;
    counter.setAttribute("aria-label", `${count} ${count === 1 ? "producto" : "productos"}`);
    counter.closest("a").setAttribute("aria-label", `Ver carrito, ${count} ${count === 1 ? "producto" : "productos"}`);
    quantity.textContent = count;
}

function removeItem(item) {
    const position = cart.indexOf(item);
    cart.splice(position, 1);
    if (!save()) cart.splice(position, 0, item);
    else showNotice("Producto quitado del carrito.");
    render();
}

function changeQuantity(item, delta, action) {
    const product = products.get(Number(item.id));
    if (!product) return;
    const next = item.cantidad + delta;
    if (next > product.stock) {
        showNotice(`Ya tienes el máximo disponible de ${product.nombre}.`);
        return;
    }
    if (next < 1) {
        removeItem(item);
        return;
    }
    const previous = item.cantidad;
    item.cantidad = next;
    if (!save()) item.cantidad = previous;
    else showNotice(`Cantidad de ${product.nombre}: ${next}.`);
    render();
    productsContainer.querySelector(`[data-id="${product.id}"] [data-action="${action}"]`)?.focus();
}

function cartRow(item) {
    const product = products.get(Number(item.id));
    const row = template.content.firstElementChild.cloneNode(true);
    row.dataset.id = product.id;
    const image = row.querySelector("img");
    image.src = product.foto || "/img/tecnologia/01.jpg";
    image.alt = product.nombre;
    row.querySelector(".cart-item-category").textContent = categoryNames[product.categoria] || "Bazar";
    row.querySelector(".cart-item-name").textContent = product.nombre;
    row.querySelector(".cart-item-price").textContent = `${money.format(product.precio)} por unidad`;
    row.querySelector(".cart-item-quantity").textContent = item.cantidad;
    row.querySelector(".cart-item-subtotal").textContent = money.format(Number(product.precio) * item.cantidad);
    const decrement = row.querySelector('[data-action="decrement"]');
    const increment = row.querySelector('[data-action="increment"]');
    const remove = row.querySelector('[data-action="remove"]');
    decrement.setAttribute("aria-label", `Reducir cantidad de ${product.nombre}`);
    increment.setAttribute("aria-label", `Aumentar cantidad de ${product.nombre}`);
    remove.setAttribute("aria-label", `Quitar ${product.nombre}`);
    increment.disabled = item.cantidad >= product.stock;
    decrement.addEventListener("click", () => changeQuantity(item, -1, "decrement"));
    increment.addEventListener("click", () => changeQuantity(item, 1, "increment"));
    remove.addEventListener("click", () => removeItem(item));
    return row;
}

function render() {
    productsContainer.replaceChildren();
    const hasItems = cart.length > 0;
    empty.hidden = hasItems;
    productsContainer.hidden = !hasItems;
    clearButton.hidden = !hasItems;
    actions.hidden = !hasItems;
    updateCounter();
    let amount = 0;
    for (const item of cart) {
        const product = products.get(Number(item.id));
        amount += Number(product.precio) * item.cantidad;
        productsContainer.appendChild(cartRow(item));
    }
    total.textContent = money.format(amount);
}

async function loadCart() {
    status.textContent = "Preparando tu carrito…";
    try {
        const response = await fetch("/api/catalog", { credentials: "same-origin" });
        if (!response.ok) throw new Error("No se pudo cargar el catálogo");
        const catalog = await response.json();
        products = new Map(catalog.map(product => [product.id, product]));
        let adjusted = false;
        for (let i = cart.length - 1; i >= 0; i--) {
            const product = products.get(Number(cart[i].id));
            if (!product || product.stock < 1) {
                cart.splice(i, 1);
                adjusted = true;
            } else if (cart[i].cantidad > product.stock) {
                cart[i].cantidad = product.stock;
                adjusted = true;
            }
        }
        if (adjusted) {
            save();
            showNotice("Actualizamos tu carrito según la disponibilidad.");
        }
        status.textContent = "";
        render();
    } catch {
        status.textContent = "No pudimos cargar tu carrito. Comprueba tu conexión y vuelve a intentarlo.";
        const retry = document.createElement("button");
        retry.className = "catalog-more";
        retry.type = "button";
        retry.textContent = "Volver a intentar";
        retry.addEventListener("click", () => { retry.remove(); loadCart(); });
        status.after(retry);
    }
}

clearButton.addEventListener("click", () => {
    const previous = [...cart];
    cart.length = 0;
    if (!save()) cart.push(...previous);
    else showNotice("Carrito vaciado.");
    render();
});

checkoutForm?.addEventListener("submit", event => {
    if (cart.length === 0) {
        event.preventDefault();
        return;
    }
    checkoutForm.querySelectorAll('input[name="item"]').forEach(input => input.remove());
    for (const item of cart) {
        const input = document.createElement("input");
        input.type = "hidden";
        input.name = "item";
        input.value = `${item.id}:${item.cantidad}`;
        checkoutForm.appendChild(input);
    }
    const button = checkoutForm.querySelector("button");
    button.disabled = true;
    button.textContent = "Abriendo pago…";
});

updateCounter();
loadCart();
