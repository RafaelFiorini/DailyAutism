import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminLayout from './components/AdminLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Profissionais from './pages/Profissionais';
import Responsaveis from './pages/Responsaveis';
import Pacientes from './pages/Pacientes';
import Usuarios from './pages/Usuarios';
import Clinicas from './pages/Clinicas';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<Dashboard />} />
              <Route path="usuarios" element={<Usuarios />} />
              <Route path="clinicas" element={<Clinicas />} />
              <Route path="profissionais" element={<Profissionais />} />
              <Route path="responsaveis" element={<Responsaveis />} />
              <Route path="pacientes" element={<Pacientes />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/admin" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
