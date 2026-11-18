import api from './api.js';

export const adminService = {
  users: () => api.get('/admin/usuarios'),
  createUser: (payload) => api.post('/admin/usuarios', payload),
  setActive: (id, activo) => api.patch(`/admin/usuarios/${id}/estado`, { activo }),
  setRole: (id, rol) => api.patch(`/admin/usuarios/${id}/rol`, { rol }),
  audit: () => api.get('/admin/auditoria'),
};
