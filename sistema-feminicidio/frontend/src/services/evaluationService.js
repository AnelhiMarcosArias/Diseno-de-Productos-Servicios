import api from './api.js';

export const evaluationService = {
  listByCase: (caseId, params = {}) => api.get(`/casos/${caseId}/evaluaciones`, { params }),
  getResult: (evaluationId) => api.get(`/evaluaciones/${evaluationId}/resultado`),
  getSimulation: (evaluationId) => api.get(`/evaluaciones/${evaluationId}/simulacion`),
  runSimulation: (evaluationId, answers) => api.post(`/evaluaciones/${evaluationId}/simulacion`, { respuestas: answers }),
};
