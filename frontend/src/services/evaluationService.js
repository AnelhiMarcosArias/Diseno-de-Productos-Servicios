import api from './api.js';

export const evaluationService = {
  listByCase: (caseId, params = {}) => api.get(`/casos/${caseId}/evaluaciones`, { params }),
  getResult: (evaluationId) => api.get(`/evaluaciones/${evaluationId}/resultado`),
};
