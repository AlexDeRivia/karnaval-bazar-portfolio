const productsContainer = document.querySelector("#contenedor-productos");
const template = document.querySelector("#section-productos");
const counter = document.querySelector("#numerito");
const categories = [...document.querySelectorAll(".boton-categoria")];
const sortControl = document.querySelector("#catalog-sort");
const count = document.querySelector("#catalog-count");
const loading = document.querySelector("#catalog-loading");
const status = document.querySelector("#catalog-status");
const moreButton = document.querySelector("#catalog-more");
const notice = document.querySelector("#store-notice");
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
let products = [];
let selectedCategory = "todos";
let visibleCount = 12;
let noticeTimer;

function updateCounter() {
    const quantity = cart.reduce((sum, item) => sum + item.cantidad, 0);
    counter.textContent = quantity;
    counter.setAttribute("aria-label", `${quantity} ${quantity === 1 ? "producto" : "productos"}`);
    counter.closest("a").setAttribute("aria-label", `Ver carrito, ${quantity} ${quantity === 1 ? "producto" : "productos"}`);
}

function showNotice(message) {
    clearTimeout(noticeTimer);
    notice.textContent = message;
    notice.classList.add("visible");
    noticeTimer = setTimeout(() => notice.classList.remove("visible"), 2800);
}

function filteredProducts() {
    const selected = selectedCategory === "todos" ? [...products] : products.filter(product => product.categoria === selectedCategory);
    switch (sortControl.value) {
        case "price-asc": selected.sort((a, b) => Number(a.precio) - Number(b.precio)); break;
        case "price-desc": selected.sort((a, b) => Number(b.precio) - Number(a.precio)); break;
        case "name": selected.sort((a, b) => a.nombre.localeCompare(b.nombre, "es")); break;
        default:
            if (selectedCategory === "todos") {
                const groups = Object.keys(categoryNames).map(category => selected.filter(product => product.categoria === category));
                const ordered = [];
                while (groups.some(group => group.length)) {
                    for (const group of groups) if (group.length) ordered.push(group.shift());
                }
                ordered.push(...selected.filter(product => !categoryNames[product.categoria]));
                return ordered;
            }
    }
    return selected;
}

function addProduct(product, button) {
    const existing = cart.find(item => Number(item.id) === product.id);
    if (existing && existing.cantidad >= product.stock) {
        showNotice(`Ya tienes el máximo disponible de ${product.nombre}.`);
        return;
    }
    if (existing) existing.cantidad++;
    else cart.push({ id: product.id, cantidad: 1 });

    try {
        localStorage.setItem("productos-en-carrito", JSON.stringify(cart));
    } catch {
        if (existing) existing.cantidad--;
        else cart.pop();
        showNotice("No se pudo guardar el carrito. Inténtalo de nuevo.");
        return;
    }
    updateCounter();
    showNotice(`${product.nombre} añadido al carrito.`);
    button.firstChild.textContent = "Añadido ";
    button.setAttribute("aria-label", `${product.nombre} añadido al carrito`);
    setTimeout(() => {
        if (button.isConnected) {
            button.firstChild.textContent = "Añadir ";
            button.setAttribute("aria-label", `Añadir ${product.nombre} al carrito`);
        }
    }, 1400);
}

function productCard(product, index) {
    const card = template.content.firstElementChild.cloneNode(true);
    card.dataset.category = categoryNames[product.categoria] ? product.categoria : "otros";
    if (index === 0 || (index > 0 && index % 9 === 0)) card.classList.add("product-card--feature");
    if (index < 8) card.style.setProperty("--enter-delay", `${index * 35}ms`);

    const image = card.querySelector(".producto-imagen");
    image.src = product.foto || "/img/tecnologia/01.jpg";
    image.alt = product.nombre;
    if (index < 4) image.loading = "eager";
    card.querySelector(".product-category").textContent = categoryNames[product.categoria] || "Bazar";
    card.querySelector(".producto-titulo").textContent = product.nombre;
    card.querySelector(".producto-precio").textContent = money.format(product.precio);
    const addButton = card.querySelector(".producto-agregar");
    addButton.setAttribute("aria-label", `Añadir ${product.nombre} al carrito`);
    addButton.addEventListener("click", () => addProduct(product, addButton));
    return card;
}

function showEmpty(message, retry = false) {
    productsContainer.replaceChildren();
    const box = document.createElement("div");
    box.className = "catalog-empty";
    const heading = document.createElement("h3");
    heading.textContent = retry ? "El catálogo no pudo cargarse." : "No hay productos aquí por ahora.";
    const description = document.createElement("p");
    description.textContent = message;
    const action = document.createElement("button");
    action.type = "button";
    action.className = "catalog-more";
    action.textContent = retry ? "Volver a intentar" : "Ver todos los productos";
    action.addEventListener("click", retry ? loadProducts : () => {
        selectedCategory = "todos";
        categories.forEach(button => {
            const active = button.id === "todos";
            button.classList.toggle("active", active);
            button.setAttribute("aria-pressed", String(active));
        });
        render();
    });
    box.append(heading, description, action);
    productsContainer.append(box);
}

function render() {
    const selected = filteredProducts();
    const shown = Math.min(visibleCount, selected.length);
    count.textContent = selected.length === 0 ? "0 productos" : `Mostrando ${shown} de ${selected.length} productos`;
    moreButton.hidden = shown >= selected.length;
    productsContainer.replaceChildren();
    if (selected.length === 0) {
        showEmpty("Prueba con otra categoría o vuelve a ver todo el bazar.");
        return;
    }
    const fragment = document.createDocumentFragment();
    selected.slice(0, shown).forEach((product, index) => fragment.appendChild(productCard(product, index)));
    productsContainer.appendChild(fragment);
}

async function loadProducts() {
    loading.hidden = false;
    status.textContent = "Cargando productos…";
    moreButton.hidden = true;
    try {
        const response = await fetch("/api/catalog", { credentials: "same-origin" });
        if (!response.ok) throw new Error("No se pudo cargar el catálogo");
        products = await response.json();
        status.textContent = "";
        render();
    } catch {
        count.textContent = "Catálogo no disponible";
        status.textContent = "No se pudo cargar la selección.";
        showEmpty("Comprueba tu conexión y vuelve a intentarlo.", true);
    } finally {
        loading.hidden = true;
    }
}

categories.forEach(button => button.addEventListener("click", () => {
    selectedCategory = button.id;
    visibleCount = 12;
    categories.forEach(item => {
        const active = item === button;
        item.classList.toggle("active", active);
        item.setAttribute("aria-pressed", String(active));
    });
    render();
}));
sortControl.addEventListener("change", () => { visibleCount = 12; render(); });
moreButton.addEventListener("click", () => { visibleCount += 12; render(); });

updateCounter();
loadProducts();
