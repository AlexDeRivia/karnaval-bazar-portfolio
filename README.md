# Karnaval Bazaar

Sistema web para consultar el catálogo de productos y gestionar clientes, empleados, proveedores, productos y compras de un bazar. Está desarrollado con Spring Boot, Thymeleaf, Spring Security y JPA.

## Estado del proyecto

El perfil `demo` carga 50 productos en una base H2 en memoria. El catálogo y el carrito funcionan con esos productos; para iniciar Stripe Checkout se deben configurar las credenciales de prueba. Los datos se restablecen al reiniciar la aplicación. El panel de gestión requiere una cuenta administrativa configurada por variables de entorno.

El perfil `prod` usa PostgreSQL para conservar pedidos. Con `CHECKOUT_MODE=stripe-test`, el servidor crea una sesión de Stripe Checkout con precios calculados desde la base de datos, guarda el pedido pendiente y lo confirma solo mediante un webhook con firma válida. La página de confirmación permite descargar un PDF ficticio una vez confirmado. **No admite claves de modo activo ni emite facturas fiscales.**

## Ejecución local

Requisitos: JDK 21 o superior y acceso a Maven Central en el primer inicio.

```bash
./mvnw clean test
./mvnw spring-boot:run
```

En Windows, usa `mvnw.cmd`. Abre `http://localhost:8080/`. Para habilitar el panel, define `ADMIN_USERNAME` y `ADMIN_PASSWORD` (al menos 12 caracteres) antes de iniciar la app. No hay contraseña predeterminada. El login está en `/admin/login`.

## Configuración

| Variable | Uso |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `demo` por defecto; `prod` para PostgreSQL. |
| `PORT` | Puerto HTTP; `8080` por defecto. |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Crea o actualiza la cuenta administrativa. |
| `APP_SEED_CATALOG` | En `prod`, carga el catálogo inicial solo si la tabla de productos está vacía. |
| `JDBC_DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` | Requeridas en el perfil `prod`. |
| `CHECKOUT_MODE` | `disabled` por defecto o `stripe-test`. |
| `STRIPE_SECRET_KEY` | Clave `sk_test_` de tu cuenta Stripe; solo en el servidor. |
| `STRIPE_PUBLISHABLE_KEY` | Clave `pk_test_` de la misma cuenta. Checkout alojado no la expone al navegador, pero se valida para impedir una configuración de modo mixto. |
| `STRIPE_WEBHOOK_SECRET` | Secreto `whsec_` del endpoint de webhook o de Stripe CLI. |
| `PUBLIC_BASE_URL` | URL base HTTPS del sitio; para pruebas locales se admite `http://localhost:8080`. En Render se usa `RENDER_EXTERNAL_URL` automáticamente si no se define. |

No guardes credenciales en Git. Configura las claves de servicios externos solo mediante variables de entorno.

## Probar una compra con Stripe

1. Obtén **tus propias** claves de prueba `sk_test_` y `pk_test_` en [Stripe Dashboard](https://dashboard.stripe.com/test/apikeys). No utilices la clave del historial del repositorio anterior.
2. Instala [Stripe CLI](https://docs.stripe.com/stripe-cli) e inicia sesión con `stripe login`. Ejecuta `stripe listen --forward-to localhost:8080/stripe/webhook` y copia el secreto `whsec_` que muestra la CLI. Mantén la CLI abierta durante la prueba.
3. Configura `CHECKOUT_MODE=stripe-test`, `STRIPE_SECRET_KEY`, `STRIPE_PUBLISHABLE_KEY`, `STRIPE_WEBHOOK_SECRET` y `PUBLIC_BASE_URL=http://localhost:8080` como variables de entorno. Por ejemplo, en PowerShell:

   ```powershell
   $env:CHECKOUT_MODE = 'stripe-test'
   $env:STRIPE_SECRET_KEY = '<tu sk_test_...>'
   $env:STRIPE_PUBLISHABLE_KEY = '<tu pk_test_...>'
   $env:STRIPE_WEBHOOK_SECRET = '<el whsec_... mostrado por Stripe CLI>'
   $env:PUBLIC_BASE_URL = 'http://localhost:8080'
   .\mvnw.cmd spring-boot:run
   ```

4. Abre `http://localhost:8080/`, añade un producto, entra al carrito y selecciona **Continuar**. Usa una fecha futura, cualquier CVC de tres dígitos y una de estas [tarjetas de prueba de Stripe](https://docs.stripe.com/testing):

   | Tarjeta | Resultado esperado |
   | --- | --- |
   | `4242 4242 4242 4242` | Pago aprobado; el webhook confirma el pedido y habilita el PDF. |
   | `4000 0000 0000 0002` | Pago rechazado; no hay confirmación ni PDF. |
   | `4000 0000 0000 3220` | Solicita autenticación 3D Secure antes de completar el pago. |

La URL de retorno solo muestra el estado; no confirma el pedido por sí sola. Si el webhook aún no llegó, la página espera la confirmación. El PDF es un comprobante de muestra sin validez tributaria. Para un sitio público usa PostgreSQL persistente y registra en Stripe el webhook `https://TU_DOMINIO/stripe/webhook` con el evento `checkout.session.completed` (también se admiten eventos de pago asíncrono y expiración). El secreto del endpoint público es distinto del secreto generado por Stripe CLI.

## Despliegue

El `Dockerfile` construye un ejecutable con Java 21 y ejecuta la aplicación como usuario sin privilegios. `render.yaml` define un servicio web con PostgreSQL externo, catálogo inicial y las claves de Stripe en modo de prueba. Tras crear el servicio, registra su URL de webhook en Stripe y añade `STRIPE_WEBHOOK_SECRET` en Render para habilitar el botón de compra.

El plan gratuito de Render puede suspender el servicio tras inactividad y tardar en volver a iniciarlo. Consulta [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) para los pasos de configuración y las limitaciones del alojamiento.
