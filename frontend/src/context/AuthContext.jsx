import { createContext, useContext, useMemo, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // No se persiste una sesión porque el backend aún no ofrece un contrato de autenticación.
  const [user, setUser] = useState(null);
  const value = useMemo(() => ({
    user,
    login: () => ({ ok: false, message: 'El backend todavía no ofrece un endpoint de inicio de sesión.' }),
    logout: () => setUser(null),
  }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
