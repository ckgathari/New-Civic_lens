import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI, profileAPI, locationsAPI } from '../api/apiClient';

const CompleteProfile = () => {
  const [formData, setFormData] = useState({
    first_name: '',
    last_name: '',
    county_id: '',
    constituency_id: '',
    ward_id: ''
  });
  const [counties, setCounties] = useState([]);
  const [constituencies, setConstituencies] = useState([]);
  const [wards, setWards] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchCounties();
  }, []);

  const fetchCounties = async () => {
    try {
      const resp = await locationsAPI.getCounties();
      setCounties(resp.data || []);
    } catch (err) {
      console.error('Error loading counties', err);
    }
  };

  const fetchConstituencies = async (countyId) => {
    setConstituencies([]);
    setWards([]);
    try {
      const resp = await locationsAPI.getConstituencies(countyId);
      setConstituencies(resp.data || []);
    } catch (err) {
      console.error('Error loading constituencies', err);
    }
  };

  const fetchWards = async (constituencyId) => {
    setWards([]);
    try {
      const resp = await locationsAPI.getWards(constituencyId);
      setWards(resp.data || []);
    } catch (err) {
      console.error('Error loading wards', err);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const { data: user } = await authAPI.getCurrentUser();
      if (!user || !user.id) {
        alert('Please log in again to complete your profile.');
        setLoading(false);
        return;
      }

      const payload = {
        first_name: formData.first_name.trim(),
        last_name: formData.last_name.trim(),
        county_id: formData.county_id,
        constituency_id: formData.constituency_id,
        ward_id: formData.ward_id,
      };

      await profileAPI.completeProfile(user.id, payload);
      navigate('/dashboard');
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message || 'Could not complete profile');
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
      backgroundColor: '#eef2f3',
      padding: '20px'
    },
    form: {
      background: 'white',
      padding: '30px',
      borderRadius: '12px',
      boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
      width: '100%',
      maxWidth: '420px'
    },
    title: {
      textAlign: 'center',
      marginBottom: '20px',
      color: '#333',
      fontSize: '24px',
      fontWeight: '600'
    },
    input: {
      width: '100%',
      padding: '10px',
      marginBottom: '15px',
      border: '1px solid #ccc',
      borderRadius: '6px',
      fontSize: '15px'
    },
    button: {
      width: '100%',
      padding: '12px',
      backgroundColor: '#4CAF50',
      color: 'white',
      border: 'none',
      borderRadius: '6px',
      cursor: 'pointer',
      fontSize: '16px',
      fontWeight: '600'
    }
  };

  return (
    <div style={styles.container}>
      <form onSubmit={handleSubmit} style={styles.form}>
        <h2 style={styles.title}>Complete Your Profile</h2>

        <input
          type="text"
          name="first_name"
          placeholder="First Name"
          value={formData.first_name}
          onChange={handleChange}
          required
          style={styles.input}
        />

        <input
          type="text"
          name="last_name"
          placeholder="Last Name"
          value={formData.last_name}
          onChange={handleChange}
          required
          style={styles.input}
        />

        <select
          name="county_id"
          value={formData.county_id}
          onChange={(e) => {
            handleChange(e);
            fetchConstituencies(e.target.value);
          }}
          required
          style={styles.input}
        >
          <option value="">Select County</option>
          {counties.map((county) => (
            <option key={county.id} value={county.id}>{county.name}</option>
          ))}
        </select>

        <select
          name="constituency_id"
          value={formData.constituency_id}
          onChange={(e) => {
            handleChange(e);
            fetchWards(e.target.value);
          }}
          required
          style={styles.input}
          disabled={!formData.county_id}
        >
          <option value="">Select Constituency</option>
          {constituencies.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>

        <select
          name="ward_id"
          value={formData.ward_id}
          onChange={handleChange}
          required
          style={styles.input}
          disabled={!formData.constituency_id}
        >
          <option value="">Select Ward</option>
          {wards.map((w) => (
            <option key={w.id} value={w.id}>{w.name}</option>
          ))}
        </select>


        <button type="submit" disabled={loading} style={styles.button}>
          {loading ? 'Saving...' : 'Save Profile'}
        </button>
      </form>
    </div>
  );
};

export default CompleteProfile;
