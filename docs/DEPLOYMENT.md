# Despliegue y alojamiento

## Requisitos del flujo de compra

Los pedidos se guardan antes de enviar al comprador a Stripe y se confirman mediante un webhook firmado. El servicio público necesita una base PostgreSQL **persistente**: H2 en memoria perdería los pedidos al reiniciar y ya no podría asociar la notificación de Stripe con el pedido. El perfil `prod` usa `JDBC_DATABASE_URL`, `DB_USERNAME` y `DB_PASSWORD`.

El archivo `render.yaml` prepara un servicio web Docker en Render con `SPRING_PROFILES_ACTIVE=prod`, `CHECKOUT_MODE=stripe-test` y `APP_SEED_CATALOG=true`. El catálogo inicial se carga solo si todavía no hay productos. Las credenciales se introducen como secretos; Render proporciona la URL pública mediante `RENDER_EXTERNAL_URL`.

## Pasos en Render

1. Verifica el permiso de uso de las imágenes del catálogo; el repositorio no documenta su procedencia.
2. Crea una base PostgreSQL persistente. Puede ser Render Postgres de pago o un proveedor externo compatible; la base gratuita de Render expira después de 30 días y no sirve como almacenamiento duradero.
3. En Render, conecta el repositorio y selecciona **New → Blueprint**, rama `main`. Completa `JDBC_DATABASE_URL` con una URL JDBC (`jdbc:postgresql://host:5432/base`), `DB_USERNAME` y `DB_PASSWORD`.
4. Durante la creación del Blueprint, configura `STRIPE_SECRET_KEY` (`sk_test_`) y `STRIPE_PUBLISHABLE_KEY` (`pk_test_`) de **tu propia cuenta** Stripe. Espera a que Render asigne la URL HTTPS del servicio. Si usas un dominio propio, define `PUBLIC_BASE_URL` manualmente con ese origen, sin barra final.
5. En el Dashboard de Stripe, registra `https://TU_DOMINIO/stripe/webhook` como endpoint de prueba. Selecciona `checkout.session.completed`; para posibles métodos de pago asíncronos, añade `checkout.session.async_payment_succeeded` y `checkout.session.async_payment_failed`. Copia el secreto `whsec_` de **ese endpoint** a `STRIPE_WEBHOOK_SECRET` en Render y despliega de nuevo. El botón de compra aparecerá cuando estén configuradas todas las variables.
6. Abre el catálogo, agrega un producto y termina una compra con la tarjeta `4242 4242 4242 4242`, una fecha futura y cualquier CVC de tres dígitos. Comprueba el evento en Stripe, la confirmación del pedido y la descarga del PDF. Prueba también un pago rechazado y verifica que no se genere comprobante.

Stripe Checkout aloja el formulario de tarjeta. El código solo acepta claves de prueba; el PDF generado es informativo y no es una factura tributaria. Si Stripe no permite abrir una cuenta para tu país o entidad, el flujo de prueba puede requerir una cuenta elegible y el cobro real necesitará otro proveedor admitido. No actives claves de modo activo con este código.

## Costes y límites

Stripe permite probar la integración sin mover dinero; los pagos reales tienen comisiones por transacción. Un servicio web Free de Render puede suspenderse tras 15 minutos sin tráfico y tardar aproximadamente un minuto en reactivarse. Render Postgres Free expira a los 30 días. Para conservar los pedidos y dar una experiencia estable, elige una base persistente y valora un servicio web sin suspensión. Consulta las condiciones vigentes antes de contratar.

Fuentes: [pruebas de Stripe](https://docs.stripe.com/testing), [países admitidos por Stripe](https://stripe.com/global), [tarifas de Stripe](https://stripe.com/pricing) y [límites gratuitos de Render](https://render.com/docs/free).
