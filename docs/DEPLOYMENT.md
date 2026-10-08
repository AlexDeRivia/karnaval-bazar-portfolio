# Despliegue y alojamiento

## Opción inicial: Render Free

**Render Web Service Free con Docker y perfil `demo`.** Requiere un único servicio y no necesita una base de datos gestionada. Dentro de los límites del plan gratuito, el servicio no tiene cuota de alojamiento. El archivo `render.yaml` contiene esta configuración. El servicio puede tardar en responder después de un periodo de inactividad y los cambios en H2 se pierden al reiniciar el proceso.

Antes de publicar, revisa también el permiso de uso de las fotografías incluidas en `src/main/resources/static/img`; el repositorio no documenta su procedencia.

Pasos de despliegue:

1. Verifica que tienes permiso para publicar las imágenes del catálogo. No incluyas credenciales ni claves de servicios externos en Git.
2. Conecta el repositorio de GitHub con Render y asegúrate de que la integración tenga acceso a él.
3. En Render, selecciona **New → Blueprint**, el repositorio y la rama `main`. Revisa el servicio definido en `render.yaml`: Docker, plan Free y `SPRING_PROFILES_ACTIVE=demo`.
4. Despliega el Blueprint y comprueba `/`, `/api/catalog`, el carrito, el resumen sin cobro y la redirección del panel al login.

## Alternativas

| Opción | Cuándo elegirla | Coste y límites |
| --- | --- | --- |
| Render Free + H2 | Entorno público con datos de prueba | Sin base de datos contratada; arranque lento tras inactividad y datos efímeros. |
| Railway Free/Hobby | Si prefieres más control o una futura base de datos | Crédito gratuito pequeño; Hobby tiene cuota mensual y el consumo puede aumentar el costo. |
| Render con PostgreSQL gestionado | Si necesitas conservar datos | Requiere plan de pago para una base duradera; el PostgreSQL gratuito expira. |

## Cobros reales

Stripe no requiere una cuota para integrar o probar Checkout. Los pagos reales conllevan una comisión por transacción según el país y medio de pago. A octubre de 2026, Perú no figura en la lista de países donde Stripe permite abrir una cuenta para aceptar pagos. Para un negocio peruano hace falta evaluar un proveedor admitido y sus tarifas. No actives cobros reales con este código: se requieren webhook verificado, pedidos persistidos y reglas de inventario, además de una cuenta y una entidad elegibles.

Fuentes para comprobar condiciones antes de contratar: [países admitidos por Stripe](https://stripe.com/global), [tarifas de Stripe](https://stripe.com/pricing), [límites gratuitos de Render](https://render.com/docs/free) y [planes de Railway](https://docs.railway.com/pricing/plans).
