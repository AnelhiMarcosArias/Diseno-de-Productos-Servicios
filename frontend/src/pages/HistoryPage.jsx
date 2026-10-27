import { useCallback, useEffect, useState } from 'react';
import { ChevronLeft, ChevronRight, Eye, FileClock, RotateCw } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Button from '../components/common/Button.jsx';
import Notice from '../components/common/Notice.jsx';
import { caseService } from '../services/caseService.js';
import { evaluationService } from '../services/evaluationService.js';
import { getApiError } from '../services/api.js';

export default function HistoryPage() {
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({ status: '', department: '', province: '', district: '', from: '', to: '' });
  const [appliedFilters, setAppliedFilters] = useState({});
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expanded, setExpanded] = useState(null);
  const [evaluations, setEvaluations] = useState({});
  const [evalLoading, setEvalLoading] = useState(null);
  const navigate = useNavigate();

  const load = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const response = await caseService.list({
        page, size: 10, sort: 'fechaRegistro,desc',
        ...(appliedFilters.status ? { estado: appliedFilters.status } : {}),
        ...(appliedFilters.department ? { departamento: appliedFilters.department } : {}),
        ...(appliedFilters.province ? { provincia: appliedFilters.province } : {}),
        ...(appliedFilters.district ? { distrito: appliedFilters.district } : {}),
        ...(appliedFilters.from ? { desde: appliedFilters.from } : {}),
        ...(appliedFilters.to ? { hasta: appliedFilters.to } : {}),
      });
      setData(response.data);
    } catch (e) { setError(getApiError(e)); }
    finally { setLoading(false); }
  }, [page, appliedFilters]);
  useEffect(() => { load(); }, [load]);

  async function toggleEvaluations(id) {
    if (expanded === id) { setExpanded(null); return; }
    setExpanded(id);
    if (evaluations[id] && !evaluations[id].error) return;
    setEvalLoading(id);
    try {
      const response = await evaluationService.listByCase(id, { page: 0, size: 20, sort: 'fechaEvaluacion,desc' });
      setEvaluations(prev => ({ ...prev, [id]: response.data.content || [] }));
    } catch (e) { setEvaluations(prev => ({ ...prev, [id]: { error: getApiError(e) } })); }
    finally { setEvalLoading(null); }
  }

  const cases = data?.content || [];
  return <AppShell>
    <div className="page-heading"><div><div className="eyebrow">CONSULTA</div><h1>Historial de casos</h1><p>Consulta registros y evaluaciones asociados a tu cuenta.</p></div><Link to="/evaluacion/nueva" className="button button--primary"><span>＋</span> Registrar caso</Link></div>
    <form className="filters-card" onSubmit={event => { event.preventDefault(); setPage(0); setAppliedFilters({ status: filters.status, department: filters.department.trim(), province: filters.province.trim(), district: filters.district.trim(), from: filters.from, to: filters.to }); }}>
      <div className="filter-field"><label htmlFor="filter-status">Estado del caso</label><select id="filter-status" value={filters.status} onChange={e => setFilters(f => ({ ...f, status: e.target.value }))}><option value="">Todos los estados</option><option value="REGISTRO">Registro</option><option value="ENEVALUACION">En evaluación</option><option value="EVALUADO">Evaluado</option><option value="CERRADO">Cerrado</option></select></div>
      <FilterInput id="filter-department" label="Departamento" value={filters.department} onChange={value => setFilters(f => ({ ...f, department: value }))} placeholder="Filtrar departamento" />
      <FilterInput id="filter-province" label="Provincia" value={filters.province} onChange={value => setFilters(f => ({ ...f, province: value }))} placeholder="Filtrar provincia" />
      <FilterInput id="filter-district" label="Distrito" value={filters.district} onChange={value => setFilters(f => ({ ...f, district: value }))} placeholder="Filtrar distrito" />
      <FilterInput id="filter-from" label="Registrado desde" type="datetime-local" value={filters.from} onChange={value => setFilters(f => ({ ...f, from: value }))} />
      <FilterInput id="filter-to" label="Registrado hasta" type="datetime-local" value={filters.to} onChange={value => setFilters(f => ({ ...f, to: value }))} />
      <Button type="submit" variant="secondary"><RotateCw size={16} /> Aplicar filtros</Button>
    </form>
    <Card className="table-card">
      {loading ? <div className="loading-state"><span className="spinner" /> Cargando historial…</div> : error ? <div className="empty-state"><FileClock size={27} /><h2>No se pudo cargar el historial</h2><p>{error}</p><Button variant="secondary" onClick={load}>Reintentar</Button></div> : cases.length === 0 ? <div className="empty-state"><FileClock size={27} /><h2>No hay casos para mostrar</h2><p>Cuando existan registros asociados a tu cuenta aparecerán aquí.</p></div> : <>
        <div className="table-scroll"><table><thead><tr><th>Código</th><th>Fecha de registro</th><th>Estado</th><th>Evaluaciones</th><th><span className="sr-only">Acciones</span></th></tr></thead><tbody>{cases.map(item => <CaseRows key={item.idCaso} item={item} expanded={expanded === item.idCaso} onToggle={() => toggleEvaluations(item.idCaso)} evaluations={evaluations[item.idCaso]} evalLoading={evalLoading === item.idCaso} onResult={evaluationId => navigate(`/evaluacion/${item.idCaso}/resultado?evaluacionId=${evaluationId}`)} />)}</tbody></table></div>
        <div className="pagination"><span>{data.totalElements} {data.totalElements === 1 ? 'caso' : 'casos'} · Página {data.number + 1} de {Math.max(data.totalPages, 1)}</span><div><Button variant="secondary" disabled={data.first} onClick={() => setPage(p => Math.max(0, p - 1))} aria-label="Página anterior"><ChevronLeft size={17} /></Button><Button variant="secondary" disabled={data.last} onClick={() => setPage(p => p + 1)} aria-label="Página siguiente"><ChevronRight size={17} /></Button></div></div>
      </>}
    </Card>
  </AppShell>;
}

function CaseRows({ item, expanded, onToggle, evaluations, evalLoading, onResult }) {
  const date = item.fechaRegistro ? new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium' }).format(new Date(item.fechaRegistro)) : '—';
  return <>
    <tr><td><b className="case-code">{item.codigoCaso}</b><small className="table-subtext">Caso #{item.idCaso}</small></td><td>{date}</td><td><span className={`status-pill status-pill--${(item.estadoCaso || '').toLowerCase()}`}>{formatStatus(item.estadoCaso)}</span></td><td><span className="muted">Consultar historial</span></td><td><Button variant="ghost" className="table-action" onClick={onToggle}><Eye size={16} /> {expanded ? 'Ocultar' : 'Ver'}</Button></td></tr>
    {expanded && <tr className="expanded-row"><td colSpan="5">{evalLoading ? <span className="muted">Cargando evaluaciones…</span> : evaluations?.error ? <><Notice tone="error">{evaluations.error}</Notice><Button variant="secondary" onClick={onToggle}>Cerrar y volver a intentar</Button></> : !evaluations?.length ? <span className="muted">Este caso no tiene evaluaciones registradas.</span> : <div className="evaluation-list">{evaluations.map(evaluation => <div className="evaluation-list__item" key={evaluation.idEvaluacion}><div><b>Evaluación #{evaluation.idEvaluacion}</b><small>{formatStatus(evaluation.estadoEvaluacion)} · {evaluation.fechaEvaluacion ? new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium' }).format(new Date(evaluation.fechaEvaluacion)) : 'Fecha no informada'}</small></div><Button variant="secondary" onClick={() => onResult(evaluation.idEvaluacion)}>Consultar resultado</Button></div>)}</div>}</td></tr>}
  </>;
}

function FilterInput({ id, label, value, onChange, ...props }) {
  return <div className="filter-field"><label htmlFor={id}>{label}</label><input id={id} value={value} onChange={event => onChange(event.target.value)} {...props} /></div>;
}

function formatStatus(value = '') { return value.replaceAll('_', ' ').toLowerCase().replace(/(^|\s)\S/g, letter => letter.toUpperCase()) || 'Sin estado'; }
