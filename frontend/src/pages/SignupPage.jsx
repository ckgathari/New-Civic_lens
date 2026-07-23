
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import LocationSelector from '../components/LocationSelector';
import { authAPI } from '../api/apiClient';
import AuthPageShell from '../components/AuthPageShell';

const SignUp = () => {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    countyId: '',
    constituencyId: '',
    wardId: ''
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleStep1Submit = (e) => {
    e.preventDefault();
    setStep(2);
  };

  const handleLocationSubmit = async (countyId, constituencyId, wardId) => {
    if (!countyId || !constituencyId || !wardId) {
      setMessage('Please select county, constituency, and ward before signing up.');
      return;
    }

    setFormData((prev) => ({ ...prev, countyId, constituencyId, wardId }));
    setLoading(true);
    setMessage('');
    try {
      const response = await authAPI.signup(
        formData.email,
        formData.password,
        formData.firstName,
        formData.lastName,
        countyId,
        constituencyId,
        wardId
      );
      localStorage.setItem('authToken', response.data.token);
      localStorage.setItem('userId', response.data.user.id);
      setMessage('Sign-up successful! Redirecting...');
      setTimeout(() => navigate('/login'), 3000);
    } catch (err) {
      console.error('Signup error:', err);
      setMessage(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const inputClass =
    'w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-2 focus:ring-sky-100';

  return (
    <AuthPageShell>
      {step === 1 ? (
          <form onSubmit={handleStep1Submit} className="space-y-4">
            <h2 className="mb-2 text-center text-2xl font-bold text-slate-900">Create Account</h2>
            <p className="mb-4 text-center text-sm text-slate-500">Step 1 of 2 — Your details</p>

            <div className="grid grid-cols-2 gap-3">
              <input
                type="text"
                name="firstName"
                placeholder="First name"
                value={formData.firstName}
                onChange={handleChange}
                required
                className={inputClass}
              />
              <input
                type="text"
                name="lastName"
                placeholder="Last name"
                value={formData.lastName}
                onChange={handleChange}
                required
                className={inputClass}
              />
            </div>
            <input
              type="email"
              name="email"
              placeholder="Email"
              value={formData.email}
              onChange={handleChange}
              required
              className={inputClass}
            />
            <input
              type="password"
              name="password"
              placeholder="Password"
              value={formData.password}
              onChange={handleChange}
              required
              className={inputClass}
            />
            <button
              type="submit"
              className="w-full rounded-xl bg-sky-600 py-3 text-sm font-semibold text-white transition hover:bg-sky-700"
            >
              Next →
            </button>
            <p className="text-center text-sm text-slate-500">
              Already have an account?{' '}
              <Link to="/login" className="font-semibold text-sky-600 hover:underline">Log in</Link>
            </p>
          </form>
        ) : (
          <div>
            <button
              onClick={() => setStep(1)}
              className="mb-4 text-sm font-semibold text-sky-600 hover:underline"
            >
              ← Back
            </button>
            <h2 className="mb-1 text-center text-2xl font-bold text-slate-900">Select Your Location</h2>
            <p className="mb-6 text-center text-sm text-slate-500">Step 2 of 2 — Where are you?</p>
            <LocationSelector onSubmit={handleLocationSubmit} />
            {loading && (
              <p className="mt-4 text-center text-sm text-slate-500">Signing up...</p>
            )}
            {message && (
              <p className={`mt-4 rounded-lg px-4 py-3 text-center text-sm ${
                message.includes('successful')
                  ? 'bg-green-50 text-green-700'
                  : 'bg-red-50 text-red-600'
              }`}>
                {message}
              </p>
            )}
          </div>
        )}
    </AuthPageShell>
  );
};

export default SignUp;
