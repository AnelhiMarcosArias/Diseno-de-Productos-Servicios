import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Activity, ClipboardPlus, FileClock, LayoutDashboard, LogOut, ShieldCheck, ChartNoAxesColumn } from 'lucide-react';
import { useAuth } from '../../context/AuthContext.jsx';

const links = [
  { to: '/', label: 'Panel principal', icon: LayoutDashboard, end: true },
  { to: '/evaluacion/nueva', label: 'Registrar evaluación', icon: ClipboardPlus },
  { to: '/historial', label: 'Historial', icon: FileClock },
  { to: '/estadisticas', label: 'Estadísticas', icon: ChartNoAxesColumn },
];

export default function AppShell({ children }) {
  const { logout, user } = useAuth();
  const navigate = useNavigate();
  async function signOut() { await logout(); navigate('/login'); }
  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><span className="brand__mark"><ShieldCheck size={22} /></span><span><b>Sistema de</b><small>Evaluación del Riesgo</small></span></div>
      <div className="sidebar__label">MENÚ PRINCIPAL</div>
      <nav className="sidebar__nav" aria-label="Navegación principal">
        {links.map(({ to, label, icon: Icon, end }) => <NavLink key={to} to={to} end={end} className={({ isActive }) => `nav-link ${isActive ? 'nav-link--active' : ''}`}><Icon size={19} /><span>{label}</span></NavLink>)}
      </nav>
      <div className="sidebar__bottom"><span className="user-avatar">{(user?.name || 'U').slice(0, 1).toUpperCase()}</span><span className="user-label">{user?.name || 'Personal autorizado'}<small>{user?.role || 'Cuenta autenticada'}</small></span><button className="icon-button" onClick={signOut} aria-label="Cerrar sesión" title="Cerrar sesión"><LogOut size={18} /></button></div>
    </aside>
    <main className="main-area">
      <header className="topbar"><div className="topbar__crumb"><Activity size={16} /> Prevención y atención</div><div className="topbar__status"><span className="status-dot" /> Sistema institucional</div></header>
      <div className="page-content">{children || <Outlet />}</div>
      <footer className="page-footer">Sistema de Evaluación del Riesgo <span>•</span> Información confidencial</footer>
    </main>
  </div>;
}
