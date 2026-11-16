# Backend Spring Boot

Este backend está en `sistema-feminicidio/backend`; el frontend React está en la carpeta hermana `../frontend` y los materiales de investigación en `../Proyecto_Feminicidio_Datos`.

## Conexión local a PostgreSQL

1. Crea una base de datos PostgreSQL llamada `sistema_feminicidio` (o ajusta `DB_URL` si usarás otro nombre o puerto).
2. Copia `.env.example` como `.env` en esta carpeta.
3. Completa `DB_USERNAME` y `DB_PASSWORD` con las credenciales de tu instalación. No compartas ni subas el archivo `.env`.
4. En el mismo `.env`, configura `AUTH_BOOTSTRAP_USERNAME`, `AUTH_BOOTSTRAP_EMAIL` y una contraseña propia en `AUTH_BOOTSTRAP_PASSWORD`. Estos valores crearán una cuenta inicial de administración solo si todavía no existe esa cuenta.
5. Desde esta carpeta (`sistema-feminicidio/backend`), inicia la aplicación con `./mvnw spring-boot:run` (Linux/macOS) o `mvnw.cmd spring-boot:run` (Windows).

En desarrollo, Hibernate crea y actualiza las tablas definidas por las entidades JPA con `DB_DDL_AUTO=update`. Esto no crea la base de datos PostgreSQL; esa base debe existir antes del primer arranque. Para una entrega reproducible, conviene reemplazar la actualización automática por migraciones Flyway versionadas.

El login usa una sesión del servidor con protección CSRF y contraseñas BCrypt. La cuenta inicial se crea solo cuando sus tres valores de bootstrap están configurados y no existe ya una cuenta con ese usuario o correo. Cambiar esos valores después no cambia la contraseña de una cuenta existente.

## Estado del microservicio ML

El backend consulta el endpoint `GET /health` del servicio FastAPI mediante `ML_SERVICE_BASE_URL` (por defecto `http://localhost:8000`). El endpoint autenticado `GET /api/system/ml-service` informa si FastAPI responde y si reconoce el contrato de salud actual. Un error o contrato diferente deja `modelLoaded` y `predictionEndpointEnabled` en `false`; la consulta tiene un timeout de dos segundos y no revela excepciones internas.

Esto comprueba conectividad operativa únicamente. El servicio experimental informa que no tiene un modelo cargado y que las predicciones están deshabilitadas. La consulta no crea evaluaciones, no modifica casos y no se conecta al cuestionario de doce variables. Para levantar FastAPI desde `sistema-feminicidio/ml-service`, usa `python -m uvicorn app.main:app --reload --port 8000` con el entorno Python preparado según su README.

La predicción del producto sigue pendiente hasta contar con definiciones académicas verificadas para V1–V12 y datos adecuados para evaluar un desenlace futuro.
