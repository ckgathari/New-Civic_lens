import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../api/apiClient';

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
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-10">
      <div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-md">
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
      </div>
    </div>
  );
};

export default Login;


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

      // After login, go straight to dashboard (no separate "complete profile" step)
      navigate('/dashboard');
    } catch (err) {
      console.error('Login error:', err.message);
      setErrorMsg(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };


  return (
    <div style={styles.container}>
      <div style={styles.card}>
        {/* Logo */}
        <div style={styles.logoContainer}>
          <img
            src="/logo.png"
            alt="CivicLens Logo"
            style={styles.logo}
          />
        </div>

        {/* Headings */}
        <h2 style={styles.heading}>Welcome Back</h2>
        <p style={styles.subheading}>Login to your CivicLens account</p>

        {/* Error message */}
        {errorMsg && <p style={styles.error}>{errorMsg}</p>}

        {/* Form */}
        <form onSubmit={handleLogin}>
          <input
            type="email"
            placeholder="Email"
            style={styles.input}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <input
            type="password"
            placeholder="Password"
            style={styles.input}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <div style={styles.forgotPasswordContainer}>
            <Link to="/forgot-password" style={styles.forgotPasswordLink}>
              Forgot password?
            </Link>
          </div>

          <button
            type="submit"
            style={styles.button}
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>


        {/* Sign up prompt */}
        <p style={styles.linkText}>
          Don't have an account?{' '}
          <Link to="/signup" style={styles.link}>Sign up</Link>
        </p>
      </div>
    </div>
  );
};

// Inline styles stay exactly as before
const styles = {
  container: {
    minHeight: '100vh',
    background: '#f0f4f8',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    padding: '20px'
  },
  card: {
    background: 'white',
    padding: '40px',
    borderRadius: '12px',
    boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
    maxWidth: '400px',
    width: '100%',
    textAlign: 'center'
  },
  logoContainer: {
    marginBottom: '20px'
  },
  logo: {
    width: '80px',
    height: 'auto'
  },
  heading: {
    marginBottom: '10px',
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#1a1a1a'
  },
  subheading: {
    marginBottom: '20px',
    color: '#6c757d',
    fontSize: '14px'
  },
  input: {
    width: '100%',
    padding: '12px',
    margin: '10px 0',
    borderRadius: '8px',
    border: '1px solid #ccc',
    fontSize: '14px'
  },
  forgotPasswordContainer: {
    textAlign: 'right',
    marginTop: '-5px',
    marginBottom: '10px'
  },
  forgotPasswordLink: {
    fontSize: '13px',
    color: '#007bff',
    textDecoration: 'none'
  },
  button: {
    width: '100%',
    padding: '12px',
    backgroundColor: '#007bff',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '16px',
    cursor: 'pointer',
    marginTop: '10px'
  },
  orDivider: {
    margin: '20px 0 10px',
    fontSize: '14px',
    color: '#999'
  },
  googleButton: {
    width: '100%',
    padding: '10px',
    backgroundColor: '#fff',
    border: '1px solid #ccc',
    borderRadius: '8px',
    fontSize: '14px',
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '10px'
  },
  googleIcon: {
    width: '18px',
    height: '18px'
  },
  linkText: {
    marginTop: '20px',
    fontSize: '14px',
    color: '#555'
  },
  link: {
    color: '#007bff',
    textDecoration: 'none',
    fontWeight: '500'
  },
  error: {
    color: '#dc3545',
    fontSize: '14px',
    marginBottom: '10px'
  }
};

export default Login;
