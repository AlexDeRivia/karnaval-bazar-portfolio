# Karnaval Bazaar

Sistema web para consultar el catálogo de productos y gestionar clientes, empleados, proveedores, productos y compras de un bazar. Está desarrollado con Spring Boot, Thymeleaf, Spring Security y JPA.

## Estado del proyecto

El perfil `demo` carga 50 productos y datos de prueba en una base H2 en memoria. El catálogo y el carrito permiten recorrer la selección de productos; el checkout muestra un resumen **sin realizar cobros ni registrar pedidos comerciales**. Los datos se restablecen al reiniciar la aplicación. El panel de gestión requiere una cuenta administrativa configurada por variables de entorno.

El perfil `prod` admite PostgreSQL, pero el checkout está desactivado por defecto. `CHECKOUT_MODE=stripe-test` solo admite una clave `sk_test_` y confirma el estado de una sesión de prueba; tampoco registra pedidos comerciales. **Este proyecto no debe usarse para ventas reales sin implementar la confirmación por webhook, persistencia de pedidos, gestión de stock y los requisitos legales y operativos del negocio.**

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
| `JDBC_DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` | Requeridas en el perfil `prod`. |
| `CHECKOUT_MODE` | En `prod`: `disabled` por defecto o `stripe-test`. |
| `STRIPE_SECRET_KEY` | Solo una clave `sk_test_` en el modo `stripe-test`; nunca en JavaScript. |
| `PUBLIC_BASE_URL` | URL pública HTTPS para los retornos de Stripe en modo de prueba. |

No guardes credenciales en Git. Configura las claves de servicios externos solo mediante variables de entorno.

## Despliegue

El `Dockerfile` construye un ejecutable con Java 21 y ejecuta la aplicación como usuario sin privilegios. `render.yaml` define un servicio web de Render con el perfil `demo` y sin base de datos externa.

Este despliegue usa datos efímeros: cada reinicio restaura el catálogo. El plan gratuito de Render puede suspender el servicio tras inactividad y tardar en volver a iniciarlo. Consulta [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) para los pasos de configuración y otras opciones de alojamiento.
