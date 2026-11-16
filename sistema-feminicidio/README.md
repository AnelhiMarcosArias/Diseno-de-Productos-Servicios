# Sistema Inteligente de Evaluación del Riesgo de Feminicidio

## Estructura

```text
sistema-feminicidio/
├── backend/                 # API y aplicación Spring Boot
├── frontend/                # Aplicación React + Vite
├── ml-service/              # Auditoría y experimento retrospectivo reproducible
└── Proyecto_Feminicidio_Datos/ # Materiales y datos de investigación existentes
```

Los datos de investigación se conservan como material de referencia. No se afirma que estén conectados al backend ni que alimenten un modelo de Machine Learning.

## Ejecutar localmente

### Backend

1. Crea la base PostgreSQL local que usa el proyecto y configura `backend/.env` a partir de `backend/.env.example`. No compartas ni subas `.env`.
2. En una terminal, entra a `sistema-feminicidio/backend` y ejecuta en Windows:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   En Linux/macOS, usa `./mvnw spring-boot:run`.

### Frontend

1. En otra terminal, entra a `sistema-feminicidio/frontend`.
2. Instala las dependencias según `package-lock.json` con `npm ci` si aún no están instaladas.
3. Inicia Vite con `npm run dev` y abre la URL local que muestre la terminal.

El frontend usa `http://localhost:8080/api` como URL de API predeterminada. Si necesitas cambiarla, copia `frontend/.env.example` a `frontend/.env` y configura `VITE_API_BASE_URL`.

## Alcance actual

El backend Spring Boot y el frontend React están separados dentro del proyecto. `ml-service` compara seis algoritmos para D107, que representa violencia severa reportada retrospectivamente; no predice feminicidio futuro ni se conecta al cuestionario. No se versiona un modelo entrenado ni existe un endpoint predictivo. El panel muestra el estado operativo de FastAPI a través de `GET /api/system/ml-service` (sesión autenticada); esa comprobación no ejecuta evaluaciones. Consulta `ml-service/README.md`, `ml-service/docs/data_audit.md` y `ml-service/docs/model_results.md` para reproducir el análisis y revisar sus límites.
