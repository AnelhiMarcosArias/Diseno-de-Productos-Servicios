# Backend Spring Boot

## Conexión local a PostgreSQL

1. Crea una base de datos PostgreSQL llamada `sistema_feminicidio` (o ajusta `DB_URL` si usarás otro nombre o puerto).
2. Copia `.env.example` como `.env` en esta carpeta.
3. Completa `DB_USERNAME` y `DB_PASSWORD` con las credenciales de tu instalación. No compartas ni subas el archivo `.env`.
4. En el mismo `.env`, configura `AUTH_BOOTSTRAP_USERNAME`, `AUTH_BOOTSTRAP_EMAIL` y una contraseña propia en `AUTH_BOOTSTRAP_PASSWORD`. Estos valores crearán una cuenta inicial de administración solo si todavía no existe esa cuenta.
5. Desde esta carpeta, inicia la aplicación con `./mvnw spring-boot:run` (Linux/macOS) o `mvnw.cmd spring-boot:run` (Windows).

En desarrollo, Hibernate crea y actualiza las tablas definidas por las entidades JPA con `DB_DDL_AUTO=update`. Esto no crea la base de datos PostgreSQL; esa base debe existir antes del primer arranque. Para una entrega reproducible, conviene reemplazar la actualización automática por migraciones Flyway versionadas.

El login usa una sesión del servidor con protección CSRF y contraseñas BCrypt. La cuenta inicial se crea solo cuando sus tres valores de bootstrap están configurados y no existe ya una cuenta con ese usuario o correo. Cambiar esos valores después no cambia la contraseña de una cuenta existente.

El microservicio de predicción sigue pendiente para la siguiente entrega.
