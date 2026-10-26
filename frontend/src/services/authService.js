/** No hay una ruta de autenticación en el backend actual. */
export async function login() {
  throw new Error('Inicio de sesión pendiente: Spring Security no expone autenticación de cuentas Usuario.');
}

export function logout() {
  // No existe token ni sesión de aplicación administrada por esta interfaz.
}
