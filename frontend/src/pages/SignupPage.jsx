
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LocationSelector from '../components/LocationSelector';
import { authAPI } from '../api/apiClient';

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
    // Validate location selection before submitting signup
    if (!countyId || !constituencyId || !wardId) {
      setMessage('Please select county, constituency, and ward before signing up.');
      return;
    }

    setFormData((prev) => ({
      ...prev,
      countyId,
      constituencyId,
      wardId
    }));
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
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    } catch (err) {
      console.error('Signup error:', err);
      setMessage(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  };

  const styles = {
    container: {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      backgroundColor: '#f4f4f4'
    },
    form: {
      background: 'white',
      padding: '30px',
      borderRadius: '10px',
      boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
      width: '350px'
    },
    title: {
      textAlign: 'center',
      marginBottom: '20px',
      color: '#333'
    },
    input: {
      width: '100%',
      padding: '10px',
      marginBottom: '10px',
      borderRadius: '5px',
      border: '1px solid #ccc'
    },
    button: {
      width: '100%',
      padding: '10px',
      backgroundColor: '#4CAF50',
      color: 'white',
      border: 'none',
      borderRadius: '5px',
      cursor: 'pointer',
      fontSize: '16px'
    },
    message: {
      marginTop: '15px',
      textAlign: 'center'
    },
    link: {
      color: '#4CAF50',
      textDecoration: 'none',
      fontWeight: 'bold'
    }
  };

  return (
    <div style={styles.container}>
      {step === 1 ? (
        <form onSubmit={handleStep1Submit} style={styles.form}>
          <h2 style={styles.title}>Sign Up</h2>
          <input
            type="text"
            name="firstName"
            placeholder="First Name"
            value={formData.firstName}
            onChange={handleChange}
            required
            style={styles.input}
          />
          <input
            type="text"
            name="lastName"
            placeholder="Last Name"
            value={formData.lastName}
            onChange={handleChange}
            required
            style={styles.input}
          />
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={formData.email}
            onChange={handleChange}
            required
            style={styles.input}
          />
          <input
            type="password"
            name="password"
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            required
            style={{ ...styles.input, marginBottom: '20px' }}
          />
          <button type="submit" style={styles.button}>
            Next
          </button>
        </form>
      ) : (
        <div style={styles.form}>
          <h2 style={styles.title}>Select Your Location</h2>
          <LocationSelector onSubmit={handleLocationSubmit} />
          {loading && <p style={styles.message}>Signing Up...</p>}
          {message && (
            <p
              style={{
                ...styles.message,
                color: message.includes('successful') ? 'green' : 'red'
              }}
            >
              {message}
            </p>
          )}
        </div>
      )}
    </div>
  );
};

export default SignUp;
