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
const variablesCuestionario = [
  { nombre: 'Celos y aislamiento', pregunta: '¿Presenta celos excesivos o intenta aislarla de amistades, estudios o familia?' },
  { nombre: 'Control de actividades', pregunta: '¿Controla su celular, redes sociales, ubicación o recursos económicos?' },
  { nombre: 'Insultos y humillación', pregunta: '¿La insulta, desvaloriza o humilla en privado o en público?' },
  { nombre: 'Intimidación', pregunta: '¿Le grita para intimidarla o destruye sus objetos personales?' },
  { nombre: 'Agresión física', pregunta: '¿La ha empujado, jaloneado o abofeteado?' },
  { nombre: 'Lesiones graves', pregunta: '¿Le ha causado lesiones graves que requirieron o ameritaron atención médica?' },
  { nombre: 'Escalamiento reciente', pregunta: '¿La violencia ha aumentado en frecuencia o intensidad durante los últimos 6 meses?' },
  { nombre: 'Alcohol o sustancias', pregunta: '¿Presenta consumo problemático de alcohol o sustancias, o antecedentes relacionados?' },
  { nombre: 'Ruptura o separación', pregunta: '¿Ha reaccionado con violencia extrema o negativa ante un intento de ruptura o separación?' },
  { nombre: 'Acoso persecutorio', pregunta: '¿La persigue o acosa en su domicilio, centro de estudios o trabajo?' },
  { nombre: 'Amenazas graves', pregunta: '¿Ha recibido amenazas explícitas de muerte, coerción grave o amenazas de suicidio por parte de la persona agresora?' },
  { nombre: 'Estrangulamiento o armas', pregunta: '¿Ha intentado estrangularla o asfixiarla, o tiene acceso a armas?' },
];

export default function EvaluationPage() {
  const navigate = useNavigate();
  const [caseData, setCaseData] = useState(() => ({ ...emptyCase, codigoCaso: createCaseCode() }));
  const [variables, setVariables] = useState(Array(12).fill(''));
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const updateCase = (event) => setCaseData(prev => ({ ...prev, [event.target.name]: event.target.value }));
  const setVariable = (index, value) => setVariables(prev => prev.map((current, i) => i === index ? value : current));

  async function submitCase(event) {
    event.preventDefault();
    setError('');
    setStep(2);
  }

  async function submitDetail(event) {
    event.preventDefault(); setError(''); setLoading(true);
    const cuestionario = Object.fromEntries(variables.map((value, i) => [`variable${i + 1}`, value === '' ? null : value === 'true']));
    const caso = {
      ...caseData,
      fechaNacimiento: caseData.fechaNacimiento || null,
      edad: caseData.edad === '' ? null : Number(caseData.edad),
      estadoCaso: 'REGISTRO',
    };
    try {
      const response = await caseService.registerWithQuestionnaire({ caso, cuestionario });
      navigate('/historial', { state: { registroGuardado: response.data } });
    } catch (e) { setError(getApiError(e)); }
    finally { setLoading(false); }
  }

  return <AppShell>
    <div className="page-heading"><div><div className="eyebrow">NUEVO REGISTRO</div><h1>Registro de caso</h1><p>Completa los datos y el cuestionario; se guardarán juntos al confirmar.</p></div><div className="heading-badge"><ShieldCheck size={17} /> Información confidencial</div></div>
    <div className="stepper"><div className={`step ${step === 1 ? 'step--active' : 'step--done'}`}><span>{step === 2 ? <Check size={15} /> : '1'}</span><b>Datos del caso</b></div><i /><div className={`step ${step === 2 ? 'step--active' : ''}`}><span>2</span><b>Cuestionario</b></div></div>
    {error && <Notice tone="error">{error}</Notice>}
    {step === 1 ? <form onSubmit={submitCase}>
      <Card className="form-card"><div className="form-card__title"><span className="section-icon"><FileText size={19} /></span><div><h2>Información del caso</h2><p>Los campos con * son obligatorios.</p></div></div>
        <div className="form-grid">
          <Field label="Código del caso" name="codigoCaso" value={caseData.codigoCaso} readOnly maxLength={50} aria-describedby="case-code-help" />
          <small id="case-code-help" className="form-hint form-grid__wide">Código único generado para este registro. Se conserva si necesitas reintentar el envío.</small>
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
      <div className="form-actions"><span className="form-hint">El caso se asociará con tu cuenta autenticada.</span><Button type="submit" disabled={loading}>Continuar al cuestionario <ArrowRight size={17} /></Button></div>
    </form> : <form onSubmit={submitDetail}>
      <Card className="form-card"><div className="form-card__title"><span className="section-icon"><ClipboardPlus size={19} /></span><div><h2>Cuestionario</h2><p>Responde los indicadores disponibles. Puedes dejar una respuesta sin marcar si no cuentas con esa información.</p></div></div>
        <div className="variables-grid">{variablesCuestionario.map(({ nombre, pregunta }, i) => <div className="variable-field" key={nombre}>
          <button className="variable-field__question" type="button" aria-describedby={`pregunta-variable-${i + 1}`}>
            <span>{nombre}</span>
            <span className="variable-field__tooltip" id={`pregunta-variable-${i + 1}`} role="tooltip">{pregunta}</span>
          </button>
          <select id={`variable-${i + 1}`} value={variables[i]} onChange={e => setVariable(i, e.target.value)} aria-label={nombre}>
            <option value="">Sin dato</option><option value="true">Sí</option><option value="false">No</option>
          </select>
        </div>)}</div>
        <Notice tone="info">“Sin dato” se guardará como respuesta ausente y “No” como respuesta negativa. Al guardar, el caso y sus respuestas se registrarán juntos. La evaluación quedará pendiente; no se generará una predicción.</Notice>
      </Card>
      <div className="form-actions"><Button type="button" variant="secondary" disabled={loading} onClick={() => setStep(1)}><ArrowLeft size={17} /> Volver a datos</Button><Button type="submit" disabled={loading}>{loading ? 'Guardando…' : <>Guardar registro <Save size={17} /></>}</Button></div>
    </form>}
  </AppShell>;
}

function createCaseCode() {
  return `CASO-${globalThis.crypto.randomUUID()}`;
}

function Field({ label, className = '', ...props }) {
  return <label className={`field ${className}`}><span>{label}</span><input {...props} /></label>;
}
