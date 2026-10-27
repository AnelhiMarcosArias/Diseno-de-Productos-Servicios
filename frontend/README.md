# Frontend

Aplicación React + Vite para el Sistema de Evaluación del Riesgo.

## Ejecutar

```sh
npm install
```

Copia `.env.example` a `.env` si el backend usa otra URL y ajusta `VITE_API_BASE_URL`. El valor local configurado es `http://localhost:8080/api`.

```sh
npm run dev
npm run build
npm run preview
```

## Integración disponible

El frontend consume los endpoints actuales de casos, detalles, evaluaciones y resultados. Las solicitudes usan Axios y manejan errores HTTP. No se persisten credenciales.

Spring Boot todavía no integra autenticación de las cuentas `Usuario`, no ofrece un endpoint de predicción ni publica estadísticas agregadas. Por ello el acceso no se simula, las rutas privadas permanecen protegidas y las estadísticas inexistentes se indican como no disponibles. La API del backend también requiere datasource y CORS configurados en Spring Boot para poder verificarse desde el navegador.
