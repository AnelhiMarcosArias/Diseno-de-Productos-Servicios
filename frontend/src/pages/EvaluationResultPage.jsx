import { useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { ArrowLeft, BadgeCheck, ClipboardList, Info, ShieldAlert } from 'lucide-react';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Notice from '../components/common/Notice.jsx';
import Button from '../components/common/Button.jsx';
import { caseService } from '../services/caseService.js';
import { evaluationService } from '../services/evaluationService.js';
import { getApiError } from '../services/api.js';

export default function EvaluationResultPage() {
  const { casoId } = useParams();
  const [searchParams] = useSearchParams();
  const evaluationId = searchParams.get('evaluacionId');
  const [caseData, setCaseData] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let alive = true;
    async function load() {
      try {
        const caseResponse = await caseService.get(casoId);
        if (alive) setCaseData(caseResponse.data);
        if (evaluationId) {
          const response = await evaluationService.getResult(evaluationId);
          if (alive) setResult(response.data);
        }
      } catch (e) { if (alive) setError(getApiError(e)); }
      finally { if (alive) setLoading(false); }
    }
    load(); return () => { alive = false; };
  }, [casoId, evaluationId]);

  const riskClass = result?.nivelRiesgo?.toLowerCase();
  const factors = result?.factoresRelevantes;
  return <AppShell>
    <div className="page-heading"><div><div className="eyebrow">CASO {caseData?.codigoCaso || `#${casoId}`}</div><h1>Resultado de evaluación</h1><p>Consulta de información disponible en el backend.</p></div><Link className="back-link" to="/historial"><ArrowLeft size={17} /> Volver al historial</Link></div>
    {loading && <Card><div className="loading-state"><span className="spinner" /> Cargando información…</div></Card>}
    {error && <Notice tone="error">{error}</Notice>}
    {!loading && !error && !result && <>
      <Card className="empty-result"><span className="empty-result__icon"><ClipboardList size={25} /></span><h2>Este caso aún no tiene un resultado</h2><p>{caseData ? `El caso ${caseData.codigoCaso} está registrado.` : 'El caso está registrado.'} El backend permite guardar variables y consultar evaluaciones, pero todavía no tiene un endpoint que ejecute el modelo ni cree una evaluación.</p><Link to={`/evaluacion/${casoId}/resultado`} className="text-link">Actualizar esta vista</Link></Card>
      <Notice tone="info">No se muestran nivel de riesgo, probabilidad ni factores porque no existe una respuesta real del modelo para este caso.</Notice>
    </>}
    {result && <>
      <Card className="result-card"><div className="result-card__top"><span className="result-card__icon"><BadgeCheck size={22} /></span><div><div className="eyebrow">NIVEL DE RIESGO ESTIMADO</div><h2 className={`risk-label risk-label--${riskClass}`}>{result.nivelRiesgo || 'No informado'}</h2></div></div>
        {result.probabilidadEstimada != null && <div className="probability"><span>Probabilidad estimada</span><b>{formatProbability(result.probabilidadEstimada)}</b></div>}
        <div className="result-meta"><span>Modelo</span><b>{result.modeloUtilizado || 'No informado'}</b><span>Fecha de generación</span><b>{formatDate(result.fechaGeneracion)}</b></div>
      </Card>
      <div className="result-columns"><Card><div className="section-heading section-heading--compact"><h2>Factores relevantes</h2></div>{factors ? <pre className="json-factors">{typeof factors === 'string' ? factors : JSON.stringify(factors, null, 2)}</pre> : <p className="muted">El resultado no incluye factores relevantes.</p>}</Card><Card><div className="section-heading section-heading--compact"><h2>Recomendaciones</h2></div><p>{result.recomendaciones || 'El resultado no incluye recomendaciones.'}</p>{result.requiereRevisionProfesional && <Notice tone="info">Se requiere revisión profesional.</Notice>}</Card></div>
      <Notice tone="info"><Info size={17} /> Esta estimación algorítmica no constituye una determinación judicial ni sustituye la evaluación de profesionales.</Notice>
    </>}
    <div className="bottom-actions"><Link to="/historial"><Button variant="secondary"><ShieldAlert size={17} /> Historial de casos</Button></Link></div>
  </AppShell>;
}

function formatDate(value) { return value ? new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'; }
function formatProbability(value) { const number = Number(value); return Number.isFinite(number) ? `${(number * 100).toLocaleString('es-PE', { maximumFractionDigits: 2 })}%` : value; }
