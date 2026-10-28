import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 12000,
  withCredentials: true,
  withXSRFToken: true,
  headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
});

export function getApiError(error) {
  if (error.response?.data?.mensaje) return error.response.data.mensaje;
  if (error.response?.status === 401) return 'La sesión no está autenticada o expiró. Inicia sesión para continuar.';
  if (error.response?.status === 403) return 'No tienes permiso para realizar esta operación.';
  if (error.response?.status === 404) return 'No se encontró el recurso solicitado.';
  if (error.code === 'ERR_NETWORK') return 'No se pudo conectar con el backend. Verifica que Spring Boot esté activo.';
  return 'Ocurrió un error al comunicarse con el servidor.';
}

export default api;
