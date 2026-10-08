# Despliegue y alojamiento

## Requisitos del flujo de compra

Los pedidos se guardan antes de enviar al comprador a Stripe y se confirman mediante un webhook firmado. La sesión de pago vence a los 30 minutos; el stock reservado vuelve al catálogo al procesar su expiración o fallo. Una tarea horaria consulta en Stripe los pedidos pendientes de más de dos horas, recupera eventos perdidos y libera reservas sin sesión. El servicio público necesita una base PostgreSQL **persistente**: H2 en memoria perdería los pedidos al reiniciar y ya no podría asociar la notificación de Stripe con el pedido. El perfil `prod` usa `JDBC_DATABASE_URL`, `DB_USERNAME` y `DB_PASSWORD`.

El archivo `render.yaml` prepara un servicio web Docker en Render con `SPRING_PROFILES_ACTIVE=prod` y `APP_SEED_CATALOG=true`. El catálogo inicial se carga solo si todavía no hay productos. Las credenciales se introducen como secretos; Render proporciona la URL pública mediante `RENDER_EXTERNAL_URL`. El checkout permanece desactivado durante el primer despliegue y se activa con `CHECKOUT_MODE=stripe-test` al registrar el webhook.

### Opción sin cargos: Render Free + Neon Free

Para conservar pedidos sin la caducidad de 30 días de Render Postgres Free, usa una base PostgreSQL de Neon Free y el servicio web Render Free. Neon mantiene los datos al suspender el cómputo por inactividad. Ambos planes tienen cuotas y pueden limitar o suspender el servicio al agotarlas; no ofrecen disponibilidad continua. El servicio web de Render se duerme tras 15 minutos sin tráfico y su siguiente visita puede tardar alrededor de un minuto. Esto también puede retrasar la recepción del webhook de Stripe; comprueba la entrega y el estado del pedido después de cada prueba.

En Render, elige el plan **Free** y no agregues un método de pago si quieres impedir cargos automáticos. Sin método de pago, Render suspende servicios o nuevas compilaciones al superar los límites en lugar de facturar. En Neon, elige el plan **Free** y no lo cambies a un plan de pago. Consulta siempre las cuotas vigentes antes de crear recursos.

## Pasos en Render

1. Verifica el permiso de uso de las imágenes del catálogo; el repositorio no documenta su procedencia.
2. Crea un proyecto en Neon Free y copia los datos de conexión de PostgreSQL. En Render, usa `JDBC_DATABASE_URL` con el formato `jdbc:postgresql://HOST/BASE?sslmode=require` (incluye el puerto si Neon lo muestra), `DB_USERNAME` con el usuario y `DB_PASSWORD` con la contraseña. Estos valores deben quedar solo en las variables privadas de Render. Render Postgres Free expira a los 30 días y no sirve para este objetivo.
3. En Render, conecta el repositorio y selecciona **New → Blueprint**, rama `main`. Elige el servicio web Free y completa las variables PostgreSQL del paso anterior.
4. Espera a que Render asigne la URL HTTPS del servicio. Si usas un dominio propio, define `PUBLIC_BASE_URL` manualmente con ese origen, sin barra final.
5. Encuentra `sk_test_` y `pk_test_` de **tu propia cuenta** en [Claves de API de Stripe](https://dashboard.stripe.com/apikeys), dentro del mismo entorno de prueba. En Stripe, registra `https://TU_DOMINIO/stripe/webhook` como endpoint de prueba. Selecciona `checkout.session.completed`, `checkout.session.expired`, `checkout.session.async_payment_succeeded` y `checkout.session.async_payment_failed`. Agrega `STRIPE_SECRET_KEY`, `STRIPE_PUBLISHABLE_KEY` y `STRIPE_WEBHOOK_SECRET` (el `whsec_` de ese endpoint) como variables privadas de Render. Cambia `CHECKOUT_MODE` a `stripe-test` y despliega de nuevo. El botón de compra aparecerá cuando estén configuradas todas las variables.
6. Agrega `ADMIN_USERNAME` y `ADMIN_PASSWORD` (12 caracteres como mínimo) como variables privadas de Render si necesitas el panel. Solo la cuenta indicada quedará activa; si omites ambas variables, ninguna cuenta administrativa podrá acceder. Abre `/admin/pedidos` para revisar estados y detalles.
7. Abre el catálogo, agrega un producto y termina una compra con la tarjeta `4242 4242 4242 4242`, una fecha futura y cualquier CVC de tres dígitos. Comprueba el evento en Stripe, la confirmación del pedido, el descuento de stock y la descarga del PDF. Prueba también un pago rechazado y verifica que no se genere comprobante.

Antes de actualizar una base con datos, crea y prueba una copia de seguridad. El perfil `prod` todavía usa `spring.jpa.hibernate.ddl-auto=update`; conviene reemplazarlo por migraciones versionadas y probarlas contra una instancia PostgreSQL antes de evolucionar el esquema en un despliegue estable.

Stripe Checkout aloja el formulario de tarjeta. El código solo acepta claves de prueba; el PDF generado es informativo y no es una factura tributaria. Si Stripe no permite abrir una cuenta para tu país o entidad, el flujo de prueba puede requerir una cuenta elegible y el cobro real necesitará otro proveedor admitido. No actives claves de modo activo con este código.

## Costes y límites

Stripe permite probar la integración sin mover dinero; los pagos reales tienen comisiones por transacción. Render Free + Neon Free evita cargos mientras permanezcas dentro de sus cuotas y no actives planes de pago. El servicio web de Render puede tardar aproximadamente un minuto en reactivarse y Neon suspende el cómputo inactivo sin borrar la base. Si necesitas respuesta inmediata y disponibilidad continua, tendrás que valorar alojamiento de pago. Consulta las condiciones vigentes antes de contratar.

Fuentes: [pruebas de Stripe](https://docs.stripe.com/testing), [claves de Stripe](https://docs.stripe.com/keys?locale=es-419), [límites gratuitos de Render](https://render.com/docs/free) y [plan gratuito de Neon](https://neon.com/blog/neon-free-plan-1-gb-per-project).
