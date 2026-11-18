import { dashboardService } from './dashboardService.js';

/** Usa el total real del listado; no infiere estadísticas que la API no ofrece. */
export async function getAvailableStatistics() {
  const response = await dashboardService.summary();
  return response.data;
}
