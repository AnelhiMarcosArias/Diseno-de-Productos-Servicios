import { useState } from 'react';
import { ShieldCheck, LockKeyhole, Mail, ArrowRight } from 'lucide-react';
import Button from '../components/common/Button.jsx';
import Notice from '../components/common/Notice.jsx';
import { login } from '../services/authService.js';

export default function LoginPage() {
  const [correo, setCorreo] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setError('');
    if (!correo.trim() || !contrasena) { setError('Completa tu correo institucional y contraseña.'); return; }
    setLoading(true);
    try { await login(correo, contrasena); }
    catch (e) { setError(e.message); }
    finally { setLoading(false); }
  }

  return <main className="login-page">
    <div className="login-decoration login-decoration--one" /><div className="login-decoration login-decoration--two" />
    <section className="login-card" aria-labelledby="login-title">
      <div className="login-brand"><span className="login-brand__icon"><ShieldCheck size={32} strokeWidth={1.8} /></span><div className="eyebrow">PLATAFORMA INSTITUCIONAL</div></div>
      <h1 id="login-title">Sistema de<br /><span>Evaluación</span></h1>
      <p className="login-subtitle">Evaluación del riesgo de feminicidio</p>
      <div className="login-divider" />
      <form onSubmit={submit} className="login-form">
        <label htmlFor="email">Correo institucional</label>
        <div className="input-wrap"><Mail size={18} /><input id="email" type="email" autoComplete="username" placeholder="nombre@institucion.gob.pe" value={correo} onChange={e => setCorreo(e.target.value)} /></div>
        <label htmlFor="password">Contraseña</label>
        <div className="input-wrap"><LockKeyhole size={18} /><input id="password" type="password" autoComplete="current-password" placeholder="Ingresa tu contraseña" value={contrasena} onChange={e => setContrasena(e.target.value)} /></div>
        {error && <Notice tone="error">{error}</Notice>}
        <Button type="submit" disabled={loading} className="login-submit">{loading ? 'Verificando…' : <>Iniciar sesión <ArrowRight size={17} /></>}</Button>
      </form>
      <p className="login-access"><LockKeyhole size={14} /> Acceso exclusivo para personal autorizado</p>
      <p className="login-pending">El backend aún no tiene autenticación de cuentas institucionales. No se enviarán ni guardarán tus credenciales.</p>
    </section>
    <div className="login-footer">© {new Date().getFullYear()} Sistema de Evaluación del Riesgo</div>
  </main>;
}
