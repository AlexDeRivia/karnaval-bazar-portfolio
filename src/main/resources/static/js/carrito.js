const empty = document.querySelector("#carrito-vacio");
const productsContainer = document.querySelector("#carrito-productos");
const actions = document.querySelector("#carrito-acciones");
const total = document.querySelector("#total");
const checkoutForm = document.querySelector("#checkout-form");
const money = new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" });

function readCart() {
    try {
        const value = JSON.parse(localStorage.getItem("productos-en-carrito"));
        return Array.isArray(value) ? value.filter(item => Number.isInteger(Number(item.id)) && Number.isInteger(item.cantidad) && item.cantidad > 0) : [];
    } catch {
        return [];
    }
}

const cart = readCart();
const response = await fetch("/api/catalog", { credentials: "same-origin" });
if (!response.ok) {
    empty.textContent = "El catálogo no está disponible. Vuelve a intentarlo.";
} else {
    const catalog = await response.json();
    const products = new Map(catalog.map(product => [product.id, product]));
    for (let i = cart.length - 1; i >= 0; i--) {
        const product = products.get(Number(cart[i].id));
        if (!product || product.stock < 1) cart.splice(i, 1);
        else cart[i].cantidad = Math.min(cart[i].cantidad, product.stock);
    }

    function save() {
        localStorage.setItem("productos-en-carrito", JSON.stringify(cart));
    }

    function render() {
        productsContainer.replaceChildren();
        const hasItems = cart.length > 0;
        empty.classList.toggle("disabled", hasItems);
        productsContainer.classList.toggle("disabled", !hasItems);
        actions.classList.toggle("disabled", !hasItems);
        let amount = 0;
        for (const item of cart) {
            const product = products.get(Number(item.id));
            amount += Number(product.precio) * item.cantidad;
            const row = document.createElement("div");
            row.className = "carrito-producto";
            row.innerHTML = '<img class="carrito-producto-imagen"><div class="carrito-producto-titulo"><small>Producto</small><h3></h3></div><div class="carrito-producto-cantidad"><small>Cantidad</small><p></p></div><div class="carrito-producto-precio"><small>Precio</small><p></p></div><div class="carrito-producto-subtotal"><small>Subtotal</small><p></p></div><button type="button" class="carrito-producto-eliminar" aria-label="Quitar producto"><i class="bi bi-trash-fill"></i></button>';
            const image = row.querySelector("img");
            image.src = product.foto;
            image.alt = product.nombre;
            row.querySelector("h3").textContent = product.nombre;
            row.querySelector(".carrito-producto-cantidad p").textContent = item.cantidad;
            row.querySelector(".carrito-producto-precio p").textContent = money.format(product.precio);
            row.querySelector(".carrito-producto-subtotal p").textContent = money.format(Number(product.precio) * item.cantidad);
            row.querySelector("button").addEventListener("click", () => {
                cart.splice(cart.indexOf(item), 1);
                save();
                render();
            });
            productsContainer.appendChild(row);
        }
        total.textContent = money.format(amount);
    }

    document.querySelector("#carrito-acciones-vaciar").addEventListener("click", () => {
        cart.length = 0;
        save();
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
    });
    save();
    render();
}
