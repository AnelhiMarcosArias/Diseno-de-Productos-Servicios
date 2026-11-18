import { Navigate, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute() {
  const { user, loading } = useAuth();
  if (loading) return <main className="auth-loading" role="status">Comprobando sesión…</main>;
  if (!user) return <Navigate to="/login" replace />;
  if (!['PROFESIONAL', 'ADMINISTRADOR'].includes(user.role)) return <AccessDenied />;
  return <Outlet />;
}

function AccessDenied() {
  const { logout } = useAuth(); const navigate = useNavigate();
  async function signOut() { await logout(); navigate('/login', { replace: true }); }
  return <main className="access-denied" role="alert"><h1>Acceso restringido</h1><p>Esta aplicación está habilitada para profesionales autorizados y administradores.</p><button className="button button--primary" onClick={signOut}>Cerrar sesión</button></main>;
}
