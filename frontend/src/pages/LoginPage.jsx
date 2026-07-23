import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../api/apiClient';
import AuthPageShell from '../components/AuthPageShell';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg('');

    try {
      const response = await authAPI.login(email, password);
      const authData = response.data;
      if (!authData || !authData.token) {
        throw new Error('Invalid login response');
      }
      localStorage.setItem('authToken', authData.token);
      navigate('/dashboard');
    } catch (err) {
      console.error('Login error:', err.message);
      setErrorMsg(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthPageShell>
      {/* Logo */}
      <div className="mb-6 flex justify-center">
        <img src="/logo.png" alt="CivicLens Logo" className="h-16 w-auto" />
      </div>

        <h2 className="mb-1 text-center text-2xl font-bold text-slate-900">Welcome Back</h2>
        <p className="mb-6 text-center text-sm text-slate-500">Login to your CivicLens account</p>

        {errorMsg && (
          <p className="mb-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">{errorMsg}</p>
        )}

        <form onSubmit={handleLogin} className="space-y-4">
          <input
            type="email"
            placeholder="Email"
            className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-2 focus:ring-sky-100"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Password"
            className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-2 focus:ring-sky-100"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <div className="text-right">
            <Link to="/forgot-password" className="text-sm text-sky-600 hover:underline">
              Forgot password?
            </Link>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-sky-600 py-3 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:opacity-60"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Don't have an account?{' '}
        <Link to="/signup" className="font-semibold text-sky-600 hover:underline">Sign up</Link>
      </p>
    </AuthPageShell>
  );
};

export default Login;
