import { Activity, FlaskConical } from 'lucide-react';
import AppShell from '../components/layout/AppShell.jsx';
import Card from '../components/common/Card.jsx';
import Notice from '../components/common/Notice.jsx';

const results = [
  { model: 'Decision Tree', cv: .9509, sd: .0033, holdout: .9483 },
  { model: 'KNN', cv: .8663, sd: .0223, holdout: .8747 },
  { model: 'Logistic Regression', cv: .9593, sd: .0047, holdout: .9623 },
  { model: 'Random Forest', cv: .9607, sd: .0045, holdout: .9611 },
  { model: 'SVM (LinearSVC)', cv: .9576, sd: .0025, holdout: .9570 },
  { model: 'XGBoost', cv: .9617, sd: .0044, holdout: .9609 },
];

export default function ResearchPage() {
  return <AppShell><div className="page-heading"><div><div className="eyebrow">MÓDULO ACADÉMICO</div><h1>Investigación de Machine Learning</h1><p>Resultados documentados del experimento ENDES 2025 y del clasificador retrospectivo D107.</p></div><span className="heading-badge"><FlaskConical size={17} /> Solo consulta académica</span></div>
    <Notice tone="info">ENDES D107 es una clasificación retrospectiva de violencia severa reportada. No predice feminicidio futuro y no se conecta con el formulario V1–V12.</Notice>
    <Card><div className="section-heading section-heading--compact"><h2>Comparación documentada de seis algoritmos</h2><p>PR-AUC sin ponderar, muestra elegible del informe versionado.</p></div><div className="table-scroll"><table><thead><tr><th>Algoritmo</th><th>PR-AUC CV agrupada</th><th>Desv. estándar</th><th>PR-AUC holdout</th></tr></thead><tbody>{results.map(row => <tr key={row.model}><td><b>{row.model}</b></td><td>{row.cv.toFixed(4)}</td><td>{row.sd.toFixed(4)}</td><td>{row.holdout.toFixed(4)}</td></tr>)}</tbody></table></div><div className="research-bars">{results.map(row => <div className="research-bar" key={row.model}><span>{row.model}<b>{row.cv.toFixed(4)}</b></span><div className="progress-track"><span style={{ width: `${row.cv * 100}%` }} /></div></div>)}</div></Card>
    <div className="result-columns research-summary"><Card><h2>Lectura del experimento</h2><p>XGBoost obtuvo el PR-AUC CV promedio más alto documentado (0.9617), mientras que Logistic Regression tuvo el mayor PR-AUC en el holdout (0.9623). La diferencia no designa un ganador para uso real.</p><p>El conjunto es transversal y el objetivo es D107. No hay desenlace longitudinal ni validación externa para evaluar feminicidio futuro.</p></Card><Card><h2>Modelo sintético del producto</h2><p>El segundo modelo XGBoost recibe V1–V12 y aprende una regla de etiqueta creada para la demostración. Sus salidas son un índice sintético 0–100, no métricas de investigación ni una predicción validada.</p><p>La regla y las limitaciones se documentan en <code>ml-service/docs/synthetic_model.md</code>.</p></Card></div>
    <Card><div className="section-heading section-heading--compact"><h2>Documentación fuente</h2></div><p>Valores reproducidos de <code>sistema-feminicidio/ml-service/docs/model_results.md</code>. Consulta también <code>ml-service/docs/data_audit.md</code> para la auditoría y el alcance del conjunto.</p><small>Esta pantalla es de consulta académica. Los valores proceden de los informes del proyecto y no se recalculan en cada visita.</small></Card>
  </AppShell>;
}
