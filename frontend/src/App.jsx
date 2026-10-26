import { Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import ProtectedRoute from './routes/ProtectedRoute.jsx';
import LoginPage from './pages/LoginPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import EvaluationPage from './pages/EvaluationPage.jsx';
import EvaluationResultPage from './pages/EvaluationResultPage.jsx';
import HistoryPage from './pages/HistoryPage.jsx';
import StatisticsPage from './pages/StatisticsPage.jsx';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/evaluacion/nueva" element={<EvaluationPage />} />
          <Route path="/evaluacion/:casoId/resultado" element={<EvaluationResultPage />} />
          <Route path="/historial" element={<HistoryPage />} />
          <Route path="/estadisticas" element={<StatisticsPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </AuthProvider>
  );
}
