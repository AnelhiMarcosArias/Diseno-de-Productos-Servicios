import { useEffect, useState } from 'react';
import { Activity, ClipboardCheck, UsersRound } from 'lucide-react';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Notice from '../components/common/Notice.jsx';
import { getAvailableStatistics } from '../services/statisticsService.js';
import { getApiError } from '../services/api.js';

export default function StatisticsPage() {
  const [cases, setCases] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => { getAvailableStatistics().then(data => setCases(data.cases)).catch(e => setError(getApiError(e))).finally(() => setLoading(false)); }, []);
  return <AppShell>
    <div className="page-heading"><div><div className="eyebrow">INDICADORES</div><h1>Estadísticas</h1><p>Resumen agregado de información disponible para tu cuenta.</p></div></div>
    {error && <Notice tone="error">{error}</Notice>}
    <div className="metrics-grid stats-grid">
      <Card className="metric-card"><div className="metric-icon metric-icon--teal"><UsersRound size={21} /></div><div><div className="metric-label">Casos registrados</div><div className="metric-value">{loading ? '…' : cases == null ? 'No disponible' : cases.toLocaleString('es-PE')}</div><div className="metric-detail">Conteo real desde el listado del backend</div></div></Card>
      <Card className="metric-card"><div className="metric-icon metric-icon--blue"><ClipboardCheck size={21} /></div><div><div className="metric-label">Evaluaciones realizadas</div><div className="metric-value metric-value--unavailable">No disponible</div><div className="metric-detail">Falta un endpoint agregado de evaluaciones</div></div></Card>
      <Card className="metric-card"><div className="metric-icon metric-icon--amber"><Activity size={21} /></div><div><div className="metric-label">Distribución de riesgo</div><div className="metric-value metric-value--unavailable">No disponible</div><div className="metric-detail">No hay predicciones ni agregaciones disponibles</div></div></Card>
    </div>
    <Card className="empty-chart"><span className="empty-chart__icon"><Activity size={24} /></span><h2>Gráficos no disponibles todavía</h2><p>El backend aún no expone agregaciones por fecha, ubicación o nivel de riesgo. Cuando existan, se podrán visualizar aquí sin usar datos ficticios.</p></Card>
  </AppShell>;
}
