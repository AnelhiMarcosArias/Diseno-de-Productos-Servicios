import { useEffect, useState } from 'react';
import { Activity, ArrowUpRight, ClipboardCheck, ClipboardList, ClipboardPlus, FileClock, ChartNoAxesColumn, ShieldCheck, UsersRound, RotateCw } from 'lucide-react';
import { Link } from 'react-router-dom';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Notice from '../components/common/Notice.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { caseService } from '../services/caseService.js';
import { dashboardService } from '../services/dashboardService.js';
import { systemStatusService } from '../services/systemStatusService.js';
import { getApiError } from '../services/api.js';

function Metric({ icon: Icon, label, value, detail, tone }) {
  return <Card className="metric-card"><div className={`metric-icon metric-icon--${tone}`}><Icon size={21} /></div><div><div className="metric-label">{label}</div><div className="metric-value">{value}</div><div className="metric-detail">{detail}</div></div></Card>;
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState(null);
  const [recentCases, setRecentCases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [mlStatus, setMlStatus] = useState(null);
  const [mlStatusLoading, setMlStatusLoading] = useState(true);
  const canViewMlStatus = ['PROFESIONAL', 'ADMINISTRADOR'].includes(user?.role);
  useEffect(() => {
    let active = true;
    Promise.all([dashboardService.summary(), caseService.list({ page: 0, size: 5, sort: 'fechaRegistro,desc' })])
      .then(([summaryResponse, caseResponse]) => { if (active) { setSummary(summaryResponse.data); setRecentCases(caseResponse.data.content || []); } })
      .catch(e => { if (active) setError(getApiError(e)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);
  useEffect(() => {
    let active = true;
    if (!canViewMlStatus) {
      setMlStatusLoading(false);
      return () => { active = false; };
    }
    systemStatusService.getMlService()
      .then(r => { if (active) setMlStatus(r.data); })
      .catch(() => { if (active) setMlStatus(null); })
      .finally(() => { if (active) setMlStatusLoading(false); });
    return () => { active = false; };
  }, [canViewMlStatus]);
  const mlStatusAvailable = mlStatus?.serviceReachable && mlStatus?.healthContractValid;
  const mlValue = mlStatusLoading ? 'Consultando…' : !canViewMlStatus ? 'Restringido' : mlStatusAvailable ? 'Activo' : 'No disponible';
  const mlDetail = !canViewMlStatus
    ? 'Estado visible para profesionales y administradores'
    : mlStatusLoading
    ? 'Verificando el servicio experimental'
    : mlStatusAvailable
      ? `Modelo sintético V1–V12: ${mlStatus.syntheticModelLoaded ? 'cargado' : 'no cargado'} · Demo, no uso real`
      : 'No se pudo verificar el servicio experimental';
  return <AppShell><div className="page-heading"><div><div className="eyebrow">RESUMEN GENERAL</div><h1>Sistema de Evaluación del Riesgo</h1><p>Panel principal</p></div><div className="heading-badge"><ShieldCheck size={17} /> Atención con enfoque preventivo</div></div>
    {error && <Notice tone="error">{error}</Notice>}
    <div className="metrics-grid">
      <Metric icon={UsersRound} label="Casos registrados" value={loading ? '…' : summary == null ? 'No disponible' : summary.casosAutorizados.toLocaleString('es-PE')} detail="Casos propios asociados a tu cuenta" tone="teal" />
      <Metric icon={ClipboardCheck} label="Simulaciones completadas" value={loading ? '…' : summary == null ? 'No disponible' : summary.simulacionesCompletadas.toLocaleString('es-PE')} detail="Resultados experimentales guardados" tone="blue" />
      <Metric icon={Activity} label="Estado del servicio ML" value={mlValue} detail={mlDetail} tone="amber" />
    </div>
    <div className="metrics-grid metrics-grid--compact"><Metric icon={FileClock} label="Evaluaciones pendientes" value={loading ? '…' : summary?.evaluacionesPendientes ?? '—'} detail="Sin índice todavía" tone="blue" /><Metric icon={RotateCw} label="Errores reintentables" value={loading ? '…' : summary?.erroresTecnicos ?? '—'} detail="Las respuestas se conservaron" tone="amber" /><Metric icon={ClipboardList} label="Evaluaciones registradas" value={loading ? '…' : summary?.evaluacionesRegistradas ?? '—'} detail="Totales en casos propios" tone="teal" /></div>
    <div className="section-heading"><div><h2>Accesos principales</h2><p>Continúa con una tarea</p></div></div>
    <div className="action-grid">
      <Link to="/evaluacion/nueva" className="action-card"><span className="action-icon action-icon--green"><ClipboardPlus size={22} /></span><span className="action-copy"><b>Registrar nuevo caso</b><small>Registra datos y variables del caso</small></span><ArrowUpRight size={19} className="action-arrow" /></Link>
      <Link to="/historial" className="action-card"><span className="action-icon action-icon--blue"><FileClock size={22} /></span><span className="action-copy"><b>Consultar historial</b><small>Revisa casos y evaluaciones asociadas</small></span><ArrowUpRight size={19} className="action-arrow" /></Link>
      <Link to="/estadisticas" className="action-card"><span className="action-icon action-icon--amber"><ChartNoAxesColumn size={22} /></span><span className="action-copy"><b>Ver estadísticas</b><small>Consulta indicadores disponibles</small></span><ArrowUpRight size={19} className="action-arrow" /></Link>
    </div>
    <Card className="recent-activity"><div className="section-heading section-heading--compact"><h2>Actividad reciente</h2><Link to="/historial">Ver historial</Link></div>{recentCases.length ? <div className="recent-list">{recentCases.map(item => <Link className="recent-list__item" to="/historial" key={item.idCaso}><span><b>{item.codigoCaso}</b><small>{item.fechaRegistro ? new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium' }).format(new Date(item.fechaRegistro)) : 'Fecha no informada'}</small></span><span className="status-pill">{(item.estadoCaso || 'REGISTRO').replaceAll('_', ' ')}</span></Link>)}</div> : !loading && <p className="muted">Aún no hay casos recientes para esta cuenta.</p>}</Card>
    <Card className="info-banner"><span className="info-banner__icon"><ShieldCheck size={20} /></span><p><b>Uso responsable</b><br />Los resultados de un modelo son estimaciones y no reemplazan la evaluación profesional ni las medidas de protección.</p></Card>
  </AppShell>;
}
