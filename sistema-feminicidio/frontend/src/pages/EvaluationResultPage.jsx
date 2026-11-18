import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useParams, useSearchParams } from 'react-router-dom';
import { ArrowLeft, BadgeCheck, ClipboardList, Download, Info, RotateCw, ShieldAlert } from 'lucide-react';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Notice from '../components/common/Notice.jsx';
import Button from '../components/common/Button.jsx';
import { caseService } from '../services/caseService.js';
import { evaluationService } from '../services/evaluationService.js';
import { getApiError } from '../services/api.js';

const questions = [
  'Celos y aislamiento', 'Control de actividades', 'Insultos y humillación', 'Intimidación',
  'Agresión física', 'Lesiones graves', 'Escalamiento reciente', 'Alcohol o sustancias',
  'Ruptura o separación', 'Acoso persecutorio', 'Amenazas graves', 'Estrangulamiento o armas',
];

export default function EvaluationResultPage() {
  const { casoId } = useParams();
  const [searchParams] = useSearchParams();
  const evaluationId = searchParams.get('evaluacionId');
  const executeNow = searchParams.get('ejecutar') === '1';
  const location = useLocation();
  const [caseData, setCaseData] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [retrying, setRetrying] = useState(false);
  const [error, setError] = useState('');

  const run = useCallback(async (answers = result?.respuestas || location.state?.respuestas) => {
    if (!evaluationId || !answers) return;
    setRetrying(true); setError('');
    try {
      const response = await evaluationService.runSimulation(evaluationId, answers);
      setResult(response.data);
    } catch (e) {
      if (e.response?.data?.estado) setResult(e.response.data);
      setError(getApiError(e));
    } finally { setRetrying(false); }
  }, [evaluationId, location.state, result?.respuestas]);

  useEffect(() => {
    let alive = true;
    async function load() {
      try {
        const caseResponse = await caseService.get(casoId);
        if (alive) setCaseData(caseResponse.data);
        if (evaluationId) {
          try {
            const response = await evaluationService.getSimulation(evaluationId);
            if (!alive) return;
            setResult(response.data);
            if (executeNow && response.data.estado !== 'COMPLETADA') await run(response.data.respuestas);
          } catch (e) {
            if (e.response?.status === 404 && alive) {
              try {
                const legacy = await evaluationService.getResult(evaluationId);
                if (alive) setResult({ ...legacy.data, legacy: true });
              } catch (legacyError) {
                if (legacyError.response?.status === 404 && location.state?.respuestas) await run(location.state.respuestas);
                else if (alive) setError(getApiError(legacyError));
              }
            } else if (alive) setError(getApiError(e));
          }
        }
      } catch (e) { if (alive) setError(getApiError(e)); }
      finally { if (alive) setLoading(false); }
    }
    load(); return () => { alive = false; };
  }, [casoId, evaluationId, executeNow]);

  const complete = result?.estado === 'COMPLETADA' && !result?.legacy;
  const critical = result?.senalesCriticas || [];
  return <AppShell>
    <div className="page-heading"><div><div className="eyebrow">CASO {caseData?.codigoCaso || `#${casoId}`}</div><h1>{result?.legacy ? 'Resultado registrado' : 'Resultado de simulación'}</h1><p>Evaluación #{evaluationId || 'sin identificar'}</p></div><Link className="back-link" to="/historial"><ArrowLeft size={17} /> Volver al historial</Link></div>
    {loading && <Card><div className="loading-state"><span className="spinner" /> Consultando la evaluación…</div></Card>}
    {error && <Notice tone="error">{error}</Notice>}
    {!loading && result?.legacy && <>
      <Card className="result-card"><div className="result-card__top"><span className="result-card__icon"><BadgeCheck size={22} /></span><div><div className="eyebrow">RESULTADO HISTÓRICO</div><h2>{result.nivelRiesgo || 'Resultado disponible'}</h2></div></div><div className="result-meta"><span>Modelo</span><b>{result.modeloUtilizado || 'No informado'}</b><span>Fecha</span><b>{formatDate(result.fechaGeneracion)}</b></div></Card>
      <Notice tone="info">Este es un resultado previo guardado con el contrato anterior. Revisa su método y alcance antes de interpretarlo.</Notice>
    </>}
    {!loading && !result && !error && <Card className="empty-result"><span className="empty-result__icon"><ClipboardList size={25} /></span><h2>No hay una evaluación asociada</h2><p>Abre este resultado desde el historial o inicia un nuevo registro.</p><Link to="/historial" className="text-link">Ir al historial</Link></Card>}
    {!loading && result?.estado === 'ERROR_TECNICO' && <Card className="empty-result"><span className="empty-result__icon"><RotateCw size={25} /></span><h2>El cálculo no se completó</h2><p>Las respuestas están guardadas en el sistema. No se generó ni se guardó un índice. Comprueba que FastAPI y el modelo sintético estén activos y vuelve a intentar.</p><Button onClick={() => run(result.respuestas)} disabled={retrying}>{retrying ? 'Reintentando…' : 'Reintentar cálculo'}</Button></Card>}
    {!loading && result?.estado === 'PENDIENTE' && <Card className="empty-result"><span className="empty-result__icon"><ClipboardList size={25} /></span><h2>La simulación está pendiente</h2><p>Las respuestas ya están guardadas. Inicia el cálculo cuando el servicio experimental esté disponible.</p><Button onClick={() => run(result.respuestas)} disabled={retrying}>{retrying ? 'Calculando…' : 'Calcular índice experimental'}</Button></Card>}
    {!loading && complete && <>
      <Card className="result-card experimental-result"><div className="result-card__top"><span className="result-card__icon"><BadgeCheck size={22} /></span><div><div className="eyebrow">ÍNDICE EXPERIMENTAL DE SEÑALES IDENTIFICADAS</div><h2 className="experimental-score">{Number(result.indiceExperimental).toFixed(2)} <small>/ 100</small></h2></div></div>
        <p>Resultado de una simulación académica mediante XGBoost con datos sintéticos. No representa un porcentaje de probabilidad, diagnóstico ni una estimación validada sobre una persona.</p>
        <div className="progress-track score-track" aria-label={`Índice experimental ${result.indiceExperimental} de 100`}><span style={{ width: `${Math.max(0, Math.min(100, Number(result.indiceExperimental)))}%` }} /></div>
        <div className="result-meta"><span>Identificador</span><b>{caseData?.codigoCaso || `Caso ${casoId}`}</b><span>Evaluación</span><b>#{evaluationId}</b><span>Modelo</span><b>{result.modeloVersion}</b><span>Fecha</span><b>{formatDate(result.fechaGeneracion)}</b></div>
        <div className="result-actions"><Button variant="secondary" onClick={() => window.print()}><Download size={17} /> Imprimir o guardar informe PDF</Button><Link to="/historial"><Button variant="secondary"><ClipboardList size={17} /> Historial</Button></Link></div>
      </Card>
      {critical.length > 0 && <Notice tone="error"><strong>Revisión profesional prioritaria:</strong> respuesta afirmativa en {critical.map(key => `${key} — ${questions[Number(key.slice(1)) - 1]}`).join('; ')}. Esta señal se muestra independientemente del índice sintético. Si existe un peligro inmediato, contacta servicios de emergencia o atención especializada de tu localidad.</Notice>}
      <Card><div className="section-heading section-heading--compact"><h2>Resumen de respuestas</h2></div><div className="simulation-answers">{questions.map((question, i) => { const key = `V${i + 1}`; const value = result.respuestas?.[key]; return <div className="simulation-answer" key={key}><span><b>{key} · {question}</b>{["V11", "V12"].includes(key) && value === 'SI' && <small className="answer-critical">Revisión prioritaria</small>}</span><strong>{value === 'SI' ? 'Sí' : value === 'NO' ? 'No' : value === 'NO_ESTOY_SEGURA' ? 'No estoy segura' : 'Sin dato'}</strong></div>; })}</div></Card>
      {result.respuestasInciertas?.length > 0 && <Notice tone="info"><Info size={17} /> Respuestas marcadas “No estoy segura”: {result.respuestasInciertas.join(', ')}. Se conservaron como categoría separada.</Notice>}
      <Notice tone="info"><Info size={17} /> {result.advertencia}</Notice>
    </>}
    <div className="bottom-actions"><Link to="/historial"><Button variant="secondary"><ShieldAlert size={17} /> Historial de casos</Button></Link></div>
  </AppShell>;
}

function formatDate(value) { return value ? new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'; }
