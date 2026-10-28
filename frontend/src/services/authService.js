import api from './api.js';

async function initializeCsrf() {
  await api.get('/auth/csrf');
}

export async function login(correo, contrasena) {
  try {
    await initializeCsrf();
    const response = await api.post('/auth/login', { correo, contrasena });
    await initializeCsrf();
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error('El correo o la contraseña no son correctos, o la cuenta está desactivada.');
    }
    throw new Error(error.response?.data?.mensaje || 'No se pudo iniciar sesión. Verifica que el backend esté activo.');
  }
}

export async function getCurrentUser() {
  const response = await api.get('/auth/me');
  return response.data;
}

export async function logout() {
  try {
    await api.post('/auth/logout');
  } catch {
    // Aunque la sesión haya expirado en el servidor, se limpia el estado local.
  }
}
