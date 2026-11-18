import { useEffect, useState } from 'react';
import { KeyRound, UserPlus, UsersRound, ClipboardList } from 'lucide-react';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Notice from '../components/common/Notice.jsx';
import { adminService } from '../services/adminService.js';
import { getApiError } from '../services/api.js';

const emptyForm = { usuario: '', contrasena: '', nombre: '', apellido: '', correo: '', rol: 'PROFESIONAL' };

export default function AdministrationPage() {
  const [users, setUsers] = useState([]); const [audit, setAudit] = useState([]);
  const [form, setForm] = useState(emptyForm); const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false); const [error, setError] = useState(''); const [success, setSuccess] = useState('');
  async function refresh() { const [u, a] = await Promise.all([adminService.users(), adminService.audit()]); setUsers(u.data); setAudit(a.data); }
  useEffect(() => { refresh().catch(e => setError(getApiError(e))).finally(() => setLoading(false)); }, []);
  async function create(event) { event.preventDefault(); setError(''); setSuccess(''); setBusy(true); try { await adminService.createUser(form); setForm(emptyForm); await refresh(); setSuccess('La cuenta se creó y quedó activa. La contraseña se guardó como hash.'); } catch (e) { setError(getApiError(e)); } finally { setBusy(false); } }
  async function toggle(user) { setError(''); setSuccess(''); try { await adminService.setActive(user.id, !user.activo); await refresh(); setSuccess('Estado de la cuenta actualizado.'); } catch (e) { setError(getApiError(e)); } }
  async function changeRole(user, role) { if (role === user.rol) return; setError(''); setSuccess(''); try { await adminService.setRole(user.id, role); await refresh(); setSuccess('Rol actualizado.'); } catch (e) { setError(getApiError(e)); } }
  return <AppShell><div className="page-heading"><div><div className="eyebrow">ADMINISTRACIÓN</div><h1>Cuentas y auditoría</h1><p>Solo administradores pueden consultar y gestionar estas funciones.</p></div></div>
    {error && <Notice tone="error">{error}</Notice>}{success && <Notice tone="success">{success}</Notice>}
    <Card className="form-card"><div className="form-card__title"><span className="section-icon"><UserPlus size={19} /></span><div><h2>Crear cuenta de personal</h2><p>La contraseña se almacena mediante BCrypt. Usa cuentas institucionales autorizadas.</p></div></div>
      <form className="admin-user-form" onSubmit={create}><Field label="Usuario" name="usuario" value={form.usuario} onChange={setForm} required maxLength={50} /><Field label="Nombre" name="nombre" value={form.nombre} onChange={setForm} required maxLength={100} /><Field label="Apellido" name="apellido" value={form.apellido} onChange={setForm} required maxLength={100} /><Field label="Correo" name="correo" type="email" value={form.correo} onChange={setForm} required maxLength={254} /><Field label="Contraseña temporal (mín. 12 caracteres)" name="contrasena" type="password" value={form.contrasena} onChange={setForm} required minLength={12} maxLength={72} autoComplete="new-password" /><label className="field"><span>Rol</span><select value={form.rol} onChange={e => setForm(f => ({ ...f, rol: e.target.value }))}><option value="PROFESIONAL">Profesional</option><option value="ADMINISTRADOR">Administrador</option></select></label><Button disabled={busy}>{busy ? 'Creando…' : <>Crear cuenta <KeyRound size={16} /></>}</Button></form>
    </Card>
    <Card className="table-card"><div className="section-heading section-heading--compact"><h2><UsersRound size={18} /> Cuentas del sistema</h2></div>{loading ? <div className="loading-state"><span className="spinner" /> Cargando cuentas…</div> : <div className="table-scroll"><table><thead><tr><th>Cuenta</th><th>Correo</th><th>Rol</th><th>Estado</th><th>Acción</th></tr></thead><tbody>{users.map(user => <tr key={user.id}><td><b>{user.nombre} {user.apellido}</b><small className="table-subtext">{user.usuario}</small></td><td>{user.correo}</td><td><select aria-label={`Rol de ${user.usuario}`} value={user.rol} onChange={e => changeRole(user, e.target.value)}><option value="PROFESIONAL">Profesional</option><option value="ADMINISTRADOR">Administrador</option><option value="PACIENTE" disabled>Paciente heredado</option></select></td><td>{user.activo ? 'Activa' : 'Desactivada'}</td><td><Button variant="secondary" onClick={() => toggle(user)}>{user.activo ? 'Desactivar' : 'Activar'}</Button></td></tr>)}</tbody></table></div>}</Card>
    <Card className="table-card"><div className="section-heading section-heading--compact"><h2><ClipboardList size={18} /> Auditoría administrativa</h2><p>Últimas 100 acciones; sin respuestas del cuestionario ni datos de personas evaluadas.</p></div><div className="table-scroll"><table><thead><tr><th>Fecha</th><th>Actor</th><th>Acción</th><th>Recurso</th><th>Referencia</th></tr></thead><tbody>{audit.map(event => <tr key={event.id}><td>{event.fechaEvento ? new Date(event.fechaEvento).toLocaleString('es-PE') : '—'}</td><td>{event.actor}</td><td>{event.accion}</td><td>{event.recurso}</td><td>{event.referencia}</td></tr>)}</tbody></table></div></Card>
  </AppShell>;
}

function Field({ label, name, value, onChange, ...props }) { return <label className="field"><span>{label}</span><input name={name} value={value} onChange={e => onChange(f => ({ ...f, [name]: e.target.value }))} {...props} /></label>; }
