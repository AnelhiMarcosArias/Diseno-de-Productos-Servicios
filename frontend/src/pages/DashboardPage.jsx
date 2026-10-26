import { useEffect, useState } from 'react';
import { ArrowUpRight, ClipboardCheck, ClipboardPlus, FileClock, ChartNoAxesColumn, ShieldCheck, UsersRound } from 'lucide-react';
import { Link } from 'react-router-dom';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Notice from '../components/common/Notice.jsx';
import { caseService } from '../services/caseService.js';
import { getApiError } from '../services/api.js';

function Metric({ icon: Icon, label, value, detail, tone }) {
  return <Card className="metric-card"><div className={`metric-icon metric-icon--${tone}`}><Icon size={21} /></div><div><div className="metric-label">{label}</div><div className="metric-value">{value}</div><div className="metric-detail">{detail}</div></div></Card>;
}

export default function DashboardPage() {
  const [total, setTotal] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => { caseService.list({ page: 0, size: 1 }).then(r => setTotal(r.data.totalElements)).catch(e => setError(getApiError(e))).finally(() => setLoading(false)); }, []);
  return <AppShell><div className="page-heading"><div><div className="eyebrow">RESUMEN GENERAL</div><h1>Sistema de Evaluación del Riesgo</h1><p>Panel principal</p></div><div className="heading-badge"><ShieldCheck size={17} /> Atención con enfoque preventivo</div></div>
    {error && <Notice tone="error">{error}</Notice>}
    <div className="metrics-grid">
      <Metric icon={UsersRound} label="Casos registrados" value={loading ? '…' : total == null ? 'No disponible' : total.toLocaleString('es-PE')} detail="Registros disponibles para tu cuenta" tone="teal" />
      <Metric icon={ClipboardCheck} label="Evaluaciones realizadas" value="No disponible" detail="El backend no ofrece un total agregado" tone="blue" />
    </div>
    <div className="section-heading"><div><h2>Accesos principales</h2><p>Continúa con una tarea</p></div></div>
    <div className="action-grid">
      <Link to="/evaluacion/nueva" className="action-card"><span className="action-icon action-icon--green"><ClipboardPlus size={22} /></span><span className="action-copy"><b>Registrar nuevo caso</b><small>Registra datos y variables del caso</small></span><ArrowUpRight size={19} className="action-arrow" /></Link>
      <Link to="/historial" className="action-card"><span className="action-icon action-icon--blue"><FileClock size={22} /></span><span className="action-copy"><b>Consultar historial</b><small>Revisa casos y evaluaciones asociadas</small></span><ArrowUpRight size={19} className="action-arrow" /></Link>
      <Link to="/estadisticas" className="action-card"><span className="action-icon action-icon--amber"><ChartNoAxesColumn size={22} /></span><span className="action-copy"><b>Ver estadísticas</b><small>Consulta indicadores disponibles</small></span><ArrowUpRight size={19} className="action-arrow" /></Link>
    </div>
    <Card className="info-banner"><span className="info-banner__icon"><ShieldCheck size={20} /></span><p><b>Uso responsable</b><br />Los resultados de un modelo son estimaciones y no reemplazan la evaluación profesional ni las medidas de protección.</p></Card>
  </AppShell>;
}
