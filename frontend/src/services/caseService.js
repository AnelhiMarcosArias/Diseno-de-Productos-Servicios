import api from './api.js';

export const caseService = {
  list: (params = {}) => api.get('/casos', { params }),
  get: (id) => api.get(`/casos/${id}`),
  create: (data) => api.post('/casos', data),
  update: (id, data) => api.put(`/casos/${id}`, data),
  getDetail: (caseId) => api.get(`/casos/${caseId}/detalle`),
  saveDetail: (caseId, data) => api.put(`/casos/${caseId}/detalle`, data),
};
