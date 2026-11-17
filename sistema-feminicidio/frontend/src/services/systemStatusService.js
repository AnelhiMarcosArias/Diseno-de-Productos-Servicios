import api from './api.js';

export const systemStatusService = {
  getMlService: () => api.get('/system/ml-service'),
};
