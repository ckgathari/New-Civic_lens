import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { authAPI } from '../api/apiClient';
import AuthPageShell from '../components/AuthPageShell';

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
    <AuthPageShell>
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
      </AuthPageShell>
    );
};

export default ForgotPassword;
