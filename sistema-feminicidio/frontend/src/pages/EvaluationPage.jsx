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
  const [answers, setAnswers] = useState(Array(12).fill(''));
  const [step, setStep] = useState(1);
  const [questionIndex, setQuestionIndex] = useState(0);
  const [review, setReview] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const updateCase = (event) => setCaseData(prev => ({ ...prev, [event.target.name]: event.target.value }));
  const setAnswer = (index, value) => setAnswers(prev => prev.map((current, i) => i === index ? value : current));

  function continueToQuestionnaire(event) {
    event.preventDefault(); setError(''); setStep(2);
  }

  async function finishEvaluation() {
    setError(''); setLoading(true);
    const answersByQuestion = Object.fromEntries(answers.map((value, i) => [`V${i + 1}`, value]));
    // Keep the legacy Boolean columns populated when they have a yes/no answer.
    // The separate simulation endpoint persists all three answer states exactly.
    const cuestionario = Object.fromEntries(answers.map((value, i) => [`variable${i + 1}`, value === 'SI' ? true : value === 'NO' ? false : null]));
    const caso = {
      ...caseData,
      fechaNacimiento: caseData.fechaNacimiento || null,
      edad: caseData.edad === '' ? null : Number(caseData.edad),
      estadoCaso: 'REGISTRO',
    };
    try {
      const response = await caseService.registerWithQuestionnaire({
        caso,
        cuestionario,
        // Keep the three answer states (SI/NO/NO_ESTOY_SEGURA) in the same
        // atomic registration as the legacy nullable Boolean columns.
        respuestasV1V12: answersByQuestion,
      });
      const saved = response.data;
      navigate(`/evaluacion/${saved.idCaso}/resultado?evaluacionId=${saved.idEvaluacion}&ejecutar=1`, { state: { respuestas: answersByQuestion } });
    } catch (e) { setError(getApiError(e)); }
    finally { setLoading(false); }
  }

  const current = variablesCuestionario[questionIndex];
  const completed = answers.filter(Boolean).length;
  return <AppShell>
    <div className="page-heading"><div><div className="eyebrow">NUEVO REGISTRO</div><h1>Registro de caso</h1><p>Registra el caso y completa una pregunta a la vez.</p></div><div className="heading-badge"><ShieldCheck size={17} /> Información confidencial</div></div>
    <div className="stepper"><div className={`step ${step === 1 ? 'step--active' : 'step--done'}`}><span>{step === 2 ? <Check size={15} /> : '1'}</span><b>Datos del caso</b></div><i /><div className={`step ${step === 2 ? 'step--active' : ''}`}><span>2</span><b>Cuestionario V1–V12</b></div></div>
    {error && <Notice tone="error">{error}</Notice>}
    {step === 1 ? <form onSubmit={continueToQuestionnaire}>
      <Card className="form-card"><div className="form-card__title"><span className="section-icon"><FileText size={19} /></span><div><h2>Información del caso</h2><p>Los campos con * son obligatorios. Evita incluir datos personales innecesarios.</p></div></div>
        <div className="form-grid">
          <Field label="Código del caso" name="codigoCaso" value={caseData.codigoCaso} readOnly maxLength={50} aria-describedby="case-code-help" />
          <small id="case-code-help" className="form-hint form-grid__wide">Código único para identificar el registro en el sistema.</small>
          <Field label="Nombre *" name="nombrePersona" value={caseData.nombrePersona} onChange={updateCase} required maxLength={100} autoComplete="off" />
          <Field label="Apellido *" name="apellidoPersona" value={caseData.apellidoPersona} onChange={updateCase} required maxLength={100} autoComplete="off" />
          <Field label="Fecha de nacimiento" name="fechaNacimiento" type="date" value={caseData.fechaNacimiento} onChange={updateCase} />
          <Field label="Edad" name="edad" type="number" min="0" max="120" value={caseData.edad} onChange={updateCase} />
          <Field label="Departamento" name="departamento" value={caseData.departamento} onChange={updateCase} maxLength={100} />
          <Field label="Provincia" name="provincia" value={caseData.provincia} onChange={updateCase} maxLength={100} />
          <Field label="Distrito" name="distrito" value={caseData.distrito} onChange={updateCase} maxLength={100} />
          <Field label="Dirección" name="direccion" value={caseData.direccion} onChange={updateCase} maxLength={300} className="form-grid__wide" />
        </div>
      </Card>
      <div className="form-actions"><span className="form-hint">El registro se asociará con tu cuenta autenticada.</span><Button type="submit">Continuar al cuestionario <ArrowRight size={17} /></Button></div>
    </form> : <>
      <Card className="form-card questionnaire-card">
        <div className="questionnaire-progress"><div><span>Pregunta {review ? 'revisión' : `${questionIndex + 1} de 12`}</span><b>{completed}/12 respondidas</b></div><div className="progress-track" role="progressbar" aria-label="Progreso del cuestionario" aria-valuemin="0" aria-valuemax="12" aria-valuenow={completed}><span style={{ width: `${completed / 12 * 100}%` }} /></div></div>
        {review ? <>
          <div className="form-card__title"><span className="section-icon"><ClipboardPlus size={19} /></span><div><h2>Revisa tus respuestas</h2><p>Confirma que las doce respuestas reflejen lo que sabes.</p></div></div>
          <div className="answer-review">{variablesCuestionario.map(({ nombre }, i) => <button className="answer-review__item" type="button" key={nombre} onClick={() => { setQuestionIndex(i); setReview(false); }}><span><b>V{i + 1} · {nombre}</b><small>{answers[i] === 'SI' ? 'Sí' : answers[i] === 'NO' ? 'No' : answers[i] === 'NO_ESTOY_SEGURA' ? 'No estoy segura' : 'Sin responder'}</small></span><span aria-hidden="true">Editar</span></button>)}</div>
          <Notice tone="info">Esta demostración emplea un modelo XGBoost entrenado con datos sintéticos. El índice no estima peligro real ni sustituye una evaluación profesional. Las respuestas de amenazas graves y estrangulamiento/acceso a armas se mostrarán aparte del índice.</Notice>
        </> : <div className="question-card" key={questionIndex}>
          <div className="eyebrow">V{questionIndex + 1} · {current.nombre}</div><h2>{current.pregunta}</h2>
          <fieldset className="answer-options"><legend>Selecciona una respuesta</legend>
            {[['SI', 'Sí'], ['NO', 'No'], ['NO_ESTOY_SEGURA', 'No estoy segura']].map(([value, label]) => <label className={`answer-option ${answers[questionIndex] === value ? 'answer-option--selected' : ''}`} key={value}><input type="radio" name={`V${questionIndex + 1}`} value={value} checked={answers[questionIndex] === value} onChange={() => setAnswer(questionIndex, value)} /><span>{label}</span></label>)}
          </fieldset>
          {!answers[questionIndex] && <p className="form-hint">Elige “No estoy segura” si no tienes información suficiente; no se interpretará como “No”.</p>}
        </div>}
      </Card>
      <div className="form-actions questionnaire-actions">
        <Button type="button" variant="secondary" disabled={loading} onClick={() => review ? setReview(false) : questionIndex === 0 ? setStep(1) : setQuestionIndex(i => i - 1)}><ArrowLeft size={17} /> {review ? 'Volver a preguntas' : questionIndex === 0 ? 'Volver a datos' : 'Anterior'}</Button>
        {review ? <Button type="button" disabled={loading || completed !== 12} onClick={finishEvaluation}>{loading ? 'Guardando y calculando…' : <>Finalizar evaluación <Save size={17} /></>}</Button>
          : questionIndex < 11 ? <Button type="button" disabled={!answers[questionIndex]} onClick={() => setQuestionIndex(i => i + 1)}>Siguiente <ArrowRight size={17} /></Button>
            : <Button type="button" disabled={!answers[questionIndex]} onClick={() => setReview(true)}>Revisar respuestas <ArrowRight size={17} /></Button>}
      </div>
    </>}
  </AppShell>;
}

function createCaseCode() { return `CASO-${globalThis.crypto.randomUUID()}`; }
function Field({ label, className = '', ...props }) { return <label className={`field ${className}`}><span>{label}</span><input {...props} /></label>; }
