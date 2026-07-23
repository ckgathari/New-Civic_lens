import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../api/apiClient';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [statusMsg, setStatusMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const handlePasswordReset = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatusMsg('');
    setErrorMsg('');

    try {
      await authAPI.forgotPassword(email);
      setStatusMsg('Check your email for the password reset link.');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || err.message);
    }

    setLoading(false);
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-10">
      <div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-md">
        {/* Logo */}
        <div className="mb-6 flex justify-center">
          <img src="/logo.png" alt="Logo" className="h-16 w-auto" />
        </div>

        <h2 className="mb-1 text-center text-2xl font-bold text-slate-900">Forgot Your Password?</h2>
        <p className="mb-6 text-center text-sm text-slate-500">
          Enter your email and we'll send you a link to reset your password.
        </p>

        {statusMsg && (
          <p className="mb-4 rounded-lg bg-green-50 px-4 py-3 text-sm text-green-700">{statusMsg}</p>
        )}
        {errorMsg && (
          <p className="mb-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600">{errorMsg}</p>
        )}

        <form onSubmit={handlePasswordReset} className="space-y-4">
          <input
            type="email"
            placeholder="Email address"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-2 focus:ring-sky-100"
          />
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-sky-600 py-3 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:opacity-60"
          >
            {loading ? 'Sending...' : 'Send Reset Link'}
          </button>
        </form>

        <div className="mt-6 text-center">
          <Link to="/login" className="text-sm font-semibold text-sky-600 hover:underline">
            ← Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;

  const [email, setEmail] = useState('');
  const [statusMsg, setStatusMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handlePasswordReset = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatusMsg('');
    setErrorMsg('');

    try {
      await authAPI.forgotPassword(email);
      setStatusMsg('Check your email for the password reset link.');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || err.message);
    }

    setLoading(false);
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        {/* Logo */}
        <div style={styles.logoContainer}>
          <img src="/logo.png" alt="Logo" style={styles.logo} />
        </div>

        <h2 style={styles.heading}>Forgot Your Password?</h2>
        <p style={styles.subheading}>
          Enter your email and we'll send you a link to reset your password.
        </p>

        {/* Feedback messages */}
        {statusMsg && <p style={styles.success}>{statusMsg}</p>}
        {errorMsg && <p style={styles.error}>{errorMsg}</p>}

        <form onSubmit={handlePasswordReset}>
          <input
            type="email"
            placeholder="Email address"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={styles.input}
          />

          <button type="submit" style={styles.button} disabled={loading}>
            {loading ? 'Sending...' : 'Send Reset Link'}
          </button>
        </form>

        <div style={styles.backToLogin}>
          <Link to="/login" style={styles.link}>
            ← Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
};

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
    background: '#fff',
    padding: '40px',
    borderRadius: '12px',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
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
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '10px',
    color: '#333'
  },
  subheading: {
    fontSize: '14px',
    color: '#777',
    marginBottom: '20px'
  },
  input: {
    width: '100%',
    padding: '12px',
    borderRadius: '8px',
    border: '1px solid #ccc',
    fontSize: '14px',
    marginBottom: '16px'
  },
  button: {
    width: '100%',
    padding: '12px',
    backgroundColor: '#007bff',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '16px',
    cursor: 'pointer'
  },
  link: {
    textDecoration: 'none',
    color: '#007bff',
    fontSize: '14px'
  },
  backToLogin: {
    marginTop: '20px'
  },
  error: {
    color: '#dc3545',
    fontSize: '14px',
    marginBottom: '10px'
  },
  success: {
    color: '#28a745',
    fontSize: '14px',
    marginBottom: '10px'
  }
};

export default ForgotPassword;
