import React from 'react';
import { useNavigate } from 'react-router-dom';
import { authAPI, profileAPI } from '../api/apiClient';
import LocationSelector from '../components/LocationSelector';

const LocationSelectorPage = () => {
  const navigate = useNavigate();

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
    <div className="flex min-h-screen items-start justify-center bg-slate-100 px-4 py-10">
      <div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-md">
        <h2 className="mb-6 text-center text-2xl font-bold text-slate-900">Select Your Location</h2>
        <LocationSelector onSubmit={handleLocationSubmit} />
      </div>
    </div>
  );
};

export default LocationSelectorPage;

