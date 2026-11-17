import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function ProtectedRoute() {
  const { user, loading } = useAuth();
  if (loading) return <main className="auth-loading" role="status">Comprobando sesión…</main>;
  return user ? <Outlet /> : <Navigate to="/login" replace />;
}
