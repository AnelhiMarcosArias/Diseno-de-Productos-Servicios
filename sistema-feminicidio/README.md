# Sistema de Evaluación — prototipo académico

Aplicación integrada con React, Spring Boot, PostgreSQL y un servicio FastAPI. El flujo de producto utiliza un XGBoost entrenado con etiquetas sintéticas solo para una demostración técnica. **No predice feminicidio ni violencia futura, no calcula probabilidad individual validada y no debe usarse para decisiones de protección.**

## Componentes y límites

- `frontend/`: login por sesión Spring, panel con conteos del usuario, registro progresivo V1–V12, historial, estadísticas, administración e investigación académica.
- `backend/`: API Spring Boot, autorización por propietario/rol, PostgreSQL, historial, simulaciones e informes imprimibles desde la vista del resultado.
- `ml-service/`: conserva la investigación ENDES D107 separada y ofrece un endpoint distinto para el modelo sintético V1–V12.
- `ml-service/docs/model_results.md`: resultados reales documentados de la comparación de seis algoritmos. XGBoost tuvo el mejor PR-AUC CV de la corrida (0.9617), pero Logistic Regression tuvo mayor PR-AUC holdout (0.9623); no se selecciona un modelo para uso real.
- `ml-service/docs/synthetic_model.md`: reglas, codificación y limitaciones del modelo de demostración V1–V12.

## Requisitos locales

- PostgreSQL instalado y servicio activo. El backend utiliza `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` de `backend/.env` (no versionar ni compartir ese archivo).
- Java 25. El backend aplica migraciones Flyway versionadas y mantiene Hibernate `update` para las tablas preexistentes del proyecto.
- Node.js/npm. `frontend/package-lock.json` fija el árbol de paquetes.
- Python 3.14 y el entorno/dependencias del microservicio.

## Arranque en Windows

Inicia PostgreSQL antes de los servicios. Ejecuta cada bloque en una terminal distinta, desde la carpeta del proyecto.

### 1. FastAPI + XGBoost sintético

```powershell
cd .\sistema-feminicidio\ml-service
.\.venv\Scripts\Activate.ps1
python .\scripts\train_synthetic_v1_v12.py
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Si no existe `.venv`, crea el entorno e instala `requirements-lock.txt` una vez. Los artefactos se generan localmente bajo `ml-service/artifacts/` y están excluidos de Git.

### 2. Spring Boot

Comprueba que `backend/.env` ya contenga la conexión local y arranca:

```powershell
cd .\sistema-feminicidio\backend
.\mvnw.cmd spring-boot:run
```

Flyway aplica las migraciones pendientes al arranque. La migración `V1__resultado_simulacion.sql` agrega las tablas `resultado_simulacion` y `auditoria`; no elimina ni actualiza casos existentes. Si ya está aplicada, Flyway indicará que el esquema está actualizado. No borres `flyway_schema_history`.

Una cuenta de administrador de desarrollo puede crearse con `AUTH_BOOTSTRAP_USERNAME`, `AUTH_BOOTSTRAP_EMAIL` y `AUTH_BOOTSTRAP_PASSWORD` en el entorno local; el servicio no modifica una cuenta que ya existe. Usa una contraseña temporal propia, cámbiala fuera del repositorio y no publiques `.env`.

### 3. Frontend

```powershell
cd .\sistema-feminicidio\frontend
npm ci
npm run dev
```

Vite usa `http://localhost:8080/api` por defecto; ajusta `VITE_API_BASE_URL` solo si tu backend escucha en otro host o puerto.

## Recorrido de evaluación

1. Inicia sesión con una cuenta activa creada previamente por administración.
2. Registra el caso y responde las 12 preguntas: Sí, No o No estoy segura.
3. Revisa las respuestas y finaliza. Caso, respuestas categóricas y evaluación se guardan asociados a la cuenta.
4. Spring llama a `POST /experimental/simulate-v1-v12` en FastAPI. El modelo devuelve un índice experimental de 0–100, que Spring guarda por evaluación en `resultado_simulacion`.
5. El historial recupera el resultado. Si FastAPI falla, las respuestas quedan conservadas con estado técnico reintentable; no se guarda un índice ficticio.
6. En la pantalla de resultado usa “Imprimir o guardar informe PDF” para generar el documento desde el navegador. La vista muestra el identificador y no necesita exponer el nombre de la persona en el informe.

El registro anterior con columnas Boolean sigue compatible. La nueva tabla conserva la opción `NO_ESTOY_SEGURA` como una categoría distinta de `NO` y de `null`.

## Endpoints principales

- Sesión: `GET /api/auth/csrf`, `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`.
- Casos: `GET /api/casos`, `GET /api/casos/{id}`, `POST /api/casos/registro-completo`.
- Evaluaciones: `GET /api/casos/{casoId}/evaluaciones`, `POST /api/evaluaciones/{evaluacionId}/simulacion`, `GET /api/evaluaciones/{evaluacionId}/simulacion`.
- Resumen: `GET /api/dashboard/resumen`.
- Administración (`ADMINISTRADOR`): `/api/admin/usuarios` y `/api/admin/auditoria`.
- Estado del servicio: `GET /api/system/ml-service`.
- FastAPI: `GET /health`, `POST /experimental/simulate-v1-v12`, `POST /experimental/classify-d107`, `/docs`.

## Pruebas locales

Desde las carpetas de cada componente:

```powershell
# Backend (requiere PostgreSQL de desarrollo para la prueba de arranque)
cd .\sistema-feminicidio\backend
.\mvnw.cmd -o test

# Frontend
cd ..\frontend
npm run build

# Python (desde ml-service, no desde .venv)
cd ..\ml-service
.\.venv\Scripts\python.exe -m pytest -q tests
```

El test de arranque del backend ejecuta Flyway sobre la base configurada. En esta instalación la primera migración ya fue aplicada y validada. No uses una base con datos reales de atención para demostraciones académicas; utiliza información sintética.
