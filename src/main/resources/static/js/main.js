const productsContainer = document.querySelector("#contenedor-productos");
const template = document.querySelector("#section-productos");
const title = document.querySelector("#titulo-principal");
const counter = document.querySelector("#numerito");
const categories = document.querySelectorAll(".boton-categoria");
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
counter.textContent = cart.reduce((sum, item) => sum + item.cantidad, 0);

function render(products) {
    productsContainer.replaceChildren();
    const fragment = document.createDocumentFragment();
    for (const product of products) {
        const card = template.content.cloneNode(true);
        const image = card.querySelector(".producto-imagen");
        image.src = product.foto || "/img/tecnologia/01.jpg";
        image.alt = product.nombre;
        card.querySelector(".producto-titulo").textContent = product.nombre;
        card.querySelector(".producto-precio").textContent = money.format(product.precio);
        card.querySelector(".producto-agregar").addEventListener("click", () => {
            const existing = cart.find(item => Number(item.id) === product.id);
            if (existing) {
                existing.cantidad = Math.min(existing.cantidad + 1, product.stock);
            } else {
                cart.push({ id: product.id, cantidad: 1 });
            }
            localStorage.setItem("productos-en-carrito", JSON.stringify(cart));
            counter.textContent = cart.reduce((sum, item) => sum + item.cantidad, 0);
        });
        fragment.appendChild(card);
    }
    productsContainer.appendChild(fragment);
}

try {
    const response = await fetch("/api/catalog", { credentials: "same-origin" });
    if (!response.ok) throw new Error("No se pudo cargar el catálogo");
    const products = await response.json();
    render(products);
    for (const button of categories) {
        button.addEventListener("click", () => {
            categories.forEach(item => item.classList.remove("active"));
            button.classList.add("active");
            const selected = button.id === "todos" ? products : products.filter(item => item.categoria === button.id);
            title.textContent = button.textContent.trim();
            render(selected);
        });
    }
} catch {
    productsContainer.textContent = "El catálogo no está disponible en este momento.";
}
