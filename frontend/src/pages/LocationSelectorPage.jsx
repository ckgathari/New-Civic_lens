import React from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI, profileAPI } from '../api/apiClient';
import LocationSelector from '../components/LocationSelector';

const LocationSelectorPage = () => {
  const navigate = useNavigate();

  // Handle the location submission
  const handleLocationSubmit = async (countyId, constituencyId, wardId) => {
    try {
      const { data: user } = await authAPI.getCurrentUser();
      if (!user || !user.id) {
        alert('No authenticated user found. Please log in.');
        navigate('/login');
        return;
      }

      await profileAPI.updateProfile(user.id, {
        county_id: countyId,
        constituency_id: constituencyId,
        ward_id: wardId
      });
      navigate('/dashboard');
    } catch (error) {
      console.error('Unexpected error:', error.message || error);
      alert('An unexpected error occurred');
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.heading}>Select Your Location</h2>
      <LocationSelector onSubmit={handleLocationSubmit} />
    </div>
  );
};

const styles = {
  container: {
    maxWidth: '600px',
    margin: '0 auto',
    padding: '20px',
    backgroundColor: '#f9f9f9',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
  },
  heading: {
    textAlign: 'center',
    marginBottom: '20px'
  }
};

export default LocationSelectorPage;
