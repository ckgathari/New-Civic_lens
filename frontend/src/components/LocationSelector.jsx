import React, { useState, useEffect } from 'react';
import { locationsAPI } from '../api/apiClient';

const selectClass =
  'w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-sky-400 focus:ring-2 focus:ring-sky-100 disabled:opacity-50';

function LocationSelector({ onSubmit }) {
  const [counties, setCounties] = useState([]);
  const [constituencies, setConstituencies] = useState([]);
  const [wards, setWards] = useState([]);

  const [selectedCounty, setSelectedCounty] = useState('');
  const [selectedConstituency, setSelectedConstituency] = useState('');
  const [selectedWard, setSelectedWard] = useState('');

  useEffect(() => {
    const fetchCounties = async () => {
      try {
        const resp = await locationsAPI.getCounties();
        setCounties(resp.data || []);
      } catch (err) {
        console.error('Error fetching counties:', err);
      }
    };
    fetchCounties();
  }, []);

  useEffect(() => {
    const fetchConstituencies = async () => {
      if (selectedCounty) {
        try {
          const resp = await locationsAPI.getConstituencies(selectedCounty);
          setConstituencies(resp.data || []);
        } catch (err) {
          console.error('Error fetching constituencies:', err);
        }
      } else {
        setConstituencies([]);
        setWards([]);
      }
    };
    fetchConstituencies();
  }, [selectedCounty]);

  useEffect(() => {
    const fetchWards = async () => {
      if (selectedConstituency) {
        try {
          const resp = await locationsAPI.getWards(selectedConstituency);
          setWards(resp.data || []);
        } catch (err) {
          console.error('Error fetching wards:', err);
        }
      } else {
        setWards([]);
      }
    };
    fetchWards();
  }, [selectedConstituency]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!onSubmit) {
      console.error('No onSubmit function provided to LocationSelector.');
      alert('Something went wrong. Please contact support.');
      return;
    }
    onSubmit(selectedCounty, selectedConstituency, selectedWard);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="mb-1 block text-sm font-semibold text-slate-700">County</label>
        <select
          value={selectedCounty}
          onChange={(e) => {
            setSelectedCounty(e.target.value);
            setSelectedConstituency('');
            setSelectedWard('');
          }}
          required
          className={selectClass}
        >
          <option value="">Select County</option>
          {counties.map((county) => (
            <option key={county.id} value={county.id}>
              {county.name}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label className="mb-1 block text-sm font-semibold text-slate-700">Constituency</label>
        <select
          value={selectedConstituency}
          onChange={(e) => {
            setSelectedConstituency(e.target.value);
            setSelectedWard('');
          }}
          required
          className={selectClass}
          disabled={!selectedCounty}
        >
          <option value="">Select Constituency</option>
          {constituencies.map((constituency) => (
            <option key={constituency.id} value={constituency.id}>
              {constituency.name}
            </option>
          ))}
        </select>
      </div>

      <div>
        <label className="mb-1 block text-sm font-semibold text-slate-700">Ward</label>
        <select
          value={selectedWard}
          onChange={(e) => setSelectedWard(e.target.value)}
          required
          className={selectClass}
          disabled={!selectedConstituency}
        >
          <option value="">Select Ward</option>
          {wards.map((ward) => (
            <option key={ward.id} value={ward.id}>
              {ward.name}
            </option>
          ))}
        </select>
      </div>

      <button
        type="submit"
        className="w-full rounded-xl bg-sky-600 py-3 text-sm font-semibold text-white transition hover:bg-sky-700"
      >
        Save Location
      </button>
    </form>
  );
}

export default LocationSelector;
