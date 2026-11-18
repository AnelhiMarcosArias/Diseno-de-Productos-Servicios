import { useEffect, useState } from 'react';
import { Activity, ClipboardCheck, UsersRound, RotateCw } from 'lucide-react';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Notice from '../components/common/Notice.jsx';
import { getAvailableStatistics } from '../services/statisticsService.js';
import { getApiError } from '../services/api.js';

export default function StatisticsPage() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => { getAvailableStatistics().then(setSummary).catch(e => setError(getApiError(e))).finally(() => setLoading(false)); }, []);
  return <AppShell>
    <div className="page-heading"><div><div className="eyebrow">INDICADORES</div><h1>Estadísticas</h1><p>Resumen agregado de información disponible para tu cuenta.</p></div></div>
    {error && <Notice tone="error">{error}</Notice>}
    <div className="metrics-grid stats-grid">
      <Metric icon={UsersRound} label="Casos registrados" value={loading ? '…' : summary?.casosAutorizados ?? 'No disponible'} detail="Casos autorizados para tu cuenta" tone="teal" />
      <Metric icon={ClipboardCheck} label="Simulaciones guardadas" value={loading ? '…' : summary?.simulacionesCompletadas ?? 'No disponible'} detail="Resultados experimentales, no clínicos" tone="blue" />
      <Metric icon={RotateCw} label="Evaluaciones con error técnico" value={loading ? '…' : summary?.erroresTecnicos ?? 'No disponible'} detail="Respuestas conservadas y reintentables" tone="amber" />
    </div>
    <Card className="empty-chart"><span className="empty-chart__icon"><Activity size={24} /></span><h2>Distribución experimental</h2>{summary && <div className="stat-bars"><Bar label="Completadas" value={summary.simulacionesCompletadas} total={summary.evaluacionesRegistradas} /><Bar label="Pendientes" value={summary.evaluacionesPendientes} total={summary.evaluacionesRegistradas} /><Bar label="Error técnico" value={summary.erroresTecnicos} total={summary.evaluacionesRegistradas} /></div>}<p>Conteos reales agregados de los casos de tu cuenta. El sistema no muestra una distribución clínica de riesgo.</p></Card>
  </AppShell>;
}

function Metric({ icon: Icon, label, value, detail, tone }) { return <Card className="metric-card"><div className={`metric-icon metric-icon--${tone}`}><Icon size={21} /></div><div><div className="metric-label">{label}</div><div className="metric-value">{Number.isFinite(value) ? value.toLocaleString('es-PE') : value}</div><div className="metric-detail">{detail}</div></div></Card>; }
function Bar({ label, value, total }) { const percent = total ? Math.round(value / total * 100) : 0; return <div className="stat-bar"><span><b>{label}</b><small>{value} · {percent}%</small></span><div className="progress-track"><span style={{ width: `${percent}%` }} /></div></div>; }
