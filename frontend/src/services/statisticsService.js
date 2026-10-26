import { caseService } from './caseService.js';

/** Usa el total real del listado; no infiere estadísticas que la API no ofrece. */
export async function getAvailableStatistics() {
  const response = await caseService.list({ page: 0, size: 1 });
  return { cases: response.data.totalElements };
}
