import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight, Check, ClipboardPlus, FileText, Save, ShieldCheck } from 'lucide-react';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Notice from '../components/common/Notice.jsx';
import { caseService } from '../services/caseService.js';
import { getApiError } from '../services/api.js';

const emptyCase = { codigoCaso: '', nombrePersona: '', apellidoPersona: '', fechaNacimiento: '', edad: '', departamento: '', provincia: '', distrito: '', direccion: '' };
const variableNames = Array.from({ length: 12 }, (_, i) => `Variable ${i + 1}`);

export default function EvaluationPage() {
  const navigate = useNavigate();
  const [caseData, setCaseData] = useState(emptyCase);
  const [variables, setVariables] = useState(Array(12).fill(''));
  const [caseId, setCaseId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const updateCase = (event) => setCaseData(prev => ({ ...prev, [event.target.name]: event.target.value }));
  const setVariable = (index, value) => setVariables(prev => prev.map((current, i) => i === index ? value : current));

  async function submitCase(event) {
    event.preventDefault(); setError(''); setNotice(''); setLoading(true);
    const body = {
      ...caseData,
      fechaNacimiento: caseData.fechaNacimiento || null,
      edad: caseData.edad === '' ? null : Number(caseData.edad),
      estadoCaso: 'REGISTRO',
    };
    try {
      const response = await caseService.create(body);
      setCaseId(response.data.idCaso);
      setNotice(`Caso ${response.data.codigoCaso} registrado. Completa sus variables para guardar el detalle.`);
    } catch (e) { setError(getApiError(e)); }
    finally { setLoading(false); }
  }

  async function submitDetail(event) {
    event.preventDefault(); setError(''); setLoading(true);
    const body = Object.fromEntries(variables.map((value, i) => [`variable${i + 1}`, value === '' ? null : value === 'true']));
    try {
      await caseService.saveDetail(caseId, body);
      navigate(`/evaluacion/${caseId}/resultado`);
    } catch (e) { setError(`El caso quedó registrado, pero no se guardaron sus variables. ${getApiError(e)}`); }
    finally { setLoading(false); }
  }

  return <AppShell>
    <div className="page-heading"><div><div className="eyebrow">NUEVO REGISTRO</div><h1>Registro de evaluación</h1><p>Complete los datos disponibles del caso y sus variables.</p></div><div className="heading-badge"><ShieldCheck size={17} /> Información confidencial</div></div>
    <div className="stepper"><div className={`step ${!caseId ? 'step--active' : 'step--done'}`}><span>{caseId ? <Check size={15} /> : '1'}</span><b>Datos del caso</b></div><i /><div className={`step ${caseId ? 'step--active' : ''}`}><span>2</span><b>Variables disponibles</b></div></div>
    {notice && <Notice tone="success">{notice}</Notice>}{error && <Notice tone="error">{error}</Notice>}
    {!caseId ? <form onSubmit={submitCase}>
      <Card className="form-card"><div className="form-card__title"><span className="section-icon"><FileText size={19} /></span><div><h2>Información del caso</h2><p>Los campos con * son obligatorios.</p></div></div>
        <div className="form-grid">
          <Field label="Código del caso *" name="codigoCaso" value={caseData.codigoCaso} onChange={updateCase} required maxLength={50} placeholder="Ej. CASO-2026-001" />
          <Field label="Nombre *" name="nombrePersona" value={caseData.nombrePersona} onChange={updateCase} required maxLength={100} />
          <Field label="Apellido *" name="apellidoPersona" value={caseData.apellidoPersona} onChange={updateCase} required maxLength={100} />
          <Field label="Fecha de nacimiento" name="fechaNacimiento" type="date" value={caseData.fechaNacimiento} onChange={updateCase} />
          <Field label="Edad" name="edad" type="number" min="0" value={caseData.edad} onChange={updateCase} />
          <Field label="Departamento" name="departamento" value={caseData.departamento} onChange={updateCase} maxLength={100} />
          <Field label="Provincia" name="provincia" value={caseData.provincia} onChange={updateCase} maxLength={100} />
          <Field label="Distrito" name="distrito" value={caseData.distrito} onChange={updateCase} maxLength={100} />
          <Field label="Dirección" name="direccion" value={caseData.direccion} onChange={updateCase} maxLength={300} className="form-grid__wide" />
        </div>
      </Card>
      <div className="form-actions"><span className="form-hint">El caso se asociará con tu cuenta autenticada.</span><Button type="submit" disabled={loading}>{loading ? 'Guardando…' : <>Registrar caso <ArrowRight size={17} /></>}</Button></div>
    </form> : <form onSubmit={submitDetail}>
      <Card className="form-card"><div className="form-card__title"><span className="section-icon"><ClipboardPlus size={19} /></span><div><h2>Variables disponibles</h2><p>El backend acepta 12 valores booleanos. Sus nombres funcionales aún no están definidos.</p></div></div>
        <div className="variables-grid">{variableNames.map((name, i) => <label className="variable-field" key={name}><span>{name}</span><select value={variables[i]} onChange={e => setVariable(i, e.target.value)} aria-label={name}><option value="">Sin dato</option><option value="true">Sí</option><option value="false">No</option></select></label>)}</div>
        <Notice tone="info">Guardar estas variables registra el detalle del caso. No ejecuta una predicción: el backend aún no dispone de un endpoint de evaluación.</Notice>
      </Card>
      <div className="form-actions"><Button type="button" variant="secondary" onClick={() => navigate('/historial')}><ArrowLeft size={17} /> Dejar para después</Button><Button type="submit" disabled={loading}>{loading ? 'Guardando…' : <>Guardar variables <Save size={17} /></>}</Button></div>
    </form>}
  </AppShell>;
}

function Field({ label, className = '', ...props }) {
  return <label className={`field ${className}`}><span>{label}</span><input {...props} /></label>;
}
