import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { authAPI, profileAPI } from './api/apiClient';
import SignupPage from './pages/SignupPage';
import LoginPage from './pages/LoginPage';
import Onboarding from './pages/Onboarding';
import LeaderPage from './pages/LeaderPage';
import AdminDashboard from './pages/AdminDashboard';
import AspirantManagement from './pages/AspirantManagement';
import CommentModeration from './pages/CommentModeration';
import LocationSelectorPage from './pages/LocationSelectorPage';
import Dashboard from './pages/Dashboard';
import LeaderCommentsPage from './pages/LeaderCommentsPage';
import ForgotPassword from './pages/ForgotPassword';
import UpdatePassword from './pages/UpdatePassword';
import CompleteProfile from './pages/CompleteProfile';

// Route wrapper to handle post-login redirection logic
function AuthRedirect() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function checkUser() {
      try {
        const token = localStorage.getItem('authToken');
        
        if (!token) {
          navigate('/login');
          return;
        }

        // Fetch current user
        const { data: user } = await authAPI.getCurrentUser();
        
        if (!user || !user.id) {
          navigate('/login');
          return;
        }

        // After login, always go to dashboard (no separate complete profile step)
        navigate('/dashboard');
      } catch (error) {
        console.error('Auth check error:', error);
        localStorage.removeItem('authToken');
        navigate('/login');
      }
    }

    checkUser().finally(() => setLoading(false));
  }, [navigate]);

  if (loading) {
    return <div>Loading...</div>;
  }

  return null;
}

function App() {
  return (
    <Router>
      <Routes>
        {/* Default route */}
        <Route path="/" element={<Navigate to="/auth-redirect" />} />

        {/* Special redirect handler */}
        <Route path="/auth-redirect" element={<AuthRedirect />} />

        {/* Auth & profile setup */}
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/complete-profile" element={<CompleteProfile />} />
        <Route path="/location-selector" element={<LocationSelectorPage />} />

        {/* App routes */}
        <Route path="/onboarding" element={<Onboarding />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/aspirants" element={<AspirantManagement />} />
        <Route path="/admin/comments" element={<CommentModeration />} />
        <Route path="/leader/:id" element={<LeaderPage />} />
        <Route path="/leader-comments/:id" element={<LeaderCommentsPage />} />

        {/* Password reset */}
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/update-password" element={<UpdatePassword />} />
      </Routes>
    </Router>
  );
}

export default App;
