# Frontend

Aplicación web React + Vite del Sistema Inteligente de Evaluación del Riesgo de Feminicidio.

## Ejecutar

Desde esta carpeta (`sistema-feminicidio/frontend`):

```sh
npm ci
npm run dev
```

Para generar la compilación de producción o previsualizarla:

```sh
npm run build
npm run preview
```

El cliente usa `http://localhost:8080/api` como URL predeterminada del backend. Para otra URL, copia `.env.example` como `.env` y define `VITE_API_BASE_URL`.

La interfaz incluye las pantallas de acceso, panel, registro/evaluación, historial, resultado y estadísticas. El backend debe estar iniciado y permitir el origen local del frontend. Las evaluaciones quedan pendientes cuando no existe un resultado real; esta aplicación no debe presentar predicciones simuladas como resultados del modelo.
