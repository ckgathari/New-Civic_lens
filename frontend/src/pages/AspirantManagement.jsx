import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminAPI, locationsAPI } from '../api/apiClient';

const AspirantManagement = () => {
  const navigate = useNavigate();
  const [aspirants, setAspirants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [darkMode, setDarkMode] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  // Location data state
  const [counties, setCounties] = useState([]);
  const [constituencies, setConstituencies] = useState([]);
  const [wards, setWards] = useState([]);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phoneNumber: '',
    position: '',
    bio: '',
    manifesto: '',
    countyId: '',
    constituencyId: '',
    wardId: '',
  });

  const positions = ['President', 'Governor', 'Senator', 'Women Rep', 'MP', 'MCA'];

  // Helper function to determine which location fields are required for a position
  const getRequiredLocations = (position) => {
    switch (position) {
      case 'President':
        return ['county'];
      case 'Governor':
      case 'Senator':
      case 'Women Rep':
        return ['county'];
      case 'MP':
        return ['county', 'constituency'];
      case 'MCA':
        return ['county', 'constituency', 'ward'];
      default:
        return [];
    }
  };

  useEffect(() => {
    loadAspirants();
    loadCounties();
  }, []);

  // Fetch counties on component mount
  const loadCounties = async () => {
    try {
      const response = await locationsAPI.getCounties();
      setCounties(response.data || []);
    } catch (err) {
      console.error('Error loading counties:', err);
    }
  };

  // Fetch constituencies when county changes
  useEffect(() => {
    if (formData.countyId) {
      loadConstituencies();
      // Reset dependent fields when county changes
      setFormData(prev => ({
        ...prev,
        constituencyId: '',
        wardId: '',
      }));
      setWards([]);
    } else {
      setConstituencies([]);
      setWards([]);
    }
  }, [formData.countyId]);

  const loadConstituencies = async () => {
    try {
      const response = await locationsAPI.getConstituencies(formData.countyId);
      setConstituencies(response.data || []);
    } catch (err) {
      console.error('Error loading constituencies:', err);
      setConstituencies([]);
    }
  };

  // Fetch wards when constituency changes
  useEffect(() => {
    if (formData.constituencyId) {
      loadWards();
      // Reset ward field when constituency changes
      setFormData(prev => ({
        ...prev,
        wardId: '',
      }));
    } else {
      setWards([]);
    }
  }, [formData.constituencyId]);

  const loadWards = async () => {
    try {
      const response = await locationsAPI.getWards(formData.constituencyId);
      setWards(response.data || []);
    } catch (err) {
      console.error('Error loading wards:', err);
      setWards([]);
    }
  };

  const loadAspirants = async () => {
    try {
      setLoading(true);
      const response = await adminAPI.getAllAspirants();
      setAspirants(response.data);
      setError(null);
    } catch (err) {
      console.error('Error loading aspirants:', err);
      setError('Failed to load aspirants');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    
    // When position changes, reset location fields
    if (name === 'position') {
      setFormData(prev => ({
        ...prev,
        [name]: value,
        countyId: '',
        constituencyId: '',
        wardId: '',
      }));
      setConstituencies([]);
      setWards([]);
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const requiredLocations = getRequiredLocations(formData.position);
      const payload = {
        ...formData,
        countyId: formData.countyId ? Number(formData.countyId) : null,
        constituencyId: formData.constituencyId ? Number(formData.constituencyId) : null,
        wardId: formData.wardId ? Number(formData.wardId) : null,
      };

      if (!requiredLocations.includes('county')) {
        payload.countyId = null;
      }
      if (!requiredLocations.includes('constituency')) {
        payload.constituencyId = null;
      }
      if (!requiredLocations.includes('ward')) {
        payload.wardId = null;
      }

      if (requiredLocations.includes('county') && !payload.countyId) {
        setError('Please select a county for this position.');
        return;
      }
      if (requiredLocations.includes('constituency') && !payload.constituencyId) {
        setError('Please select a constituency for this position.');
        return;
      }
      if (requiredLocations.includes('ward') && !payload.wardId) {
        setError('Please select a ward for this position.');
        return;
      }

      if (editingId) {
        await adminAPI.updateAspirant(editingId, payload);
      } else {
        await adminAPI.createAspirant(payload);
      }
      loadAspirants();
      resetForm();
      setError(null);
    } catch (err) {
      console.error('Error saving aspirant:', err);
      const serverError = err?.response?.data?.error;
      const fallbackMessage = err?.response?.data?.message || err?.message || 'Failed to save aspirant';
      setError(serverError || fallbackMessage);
    }
  };

  const handleEdit = (aspirant) => {
    setFormData(aspirant);
    setEditingId(aspirant.id);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this aspirant?')) {
      try {
        await adminAPI.deleteAspirant(id);
        loadAspirants();
        setError(null);
      } catch (err) {
        console.error('Error deleting aspirant:', err);
        setError('Failed to delete aspirant');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      email: '',
      phoneNumber: '',
      position: '',
      bio: '',
      manifesto: '',
      countyId: '',
      constituencyId: '',
      wardId: '',
    });
    setEditingId(null);
    setShowForm(false);
    setConstituencies([]);
    setWards([]);
  };

  const filteredAspirants = aspirants.filter(aspirant =>
    aspirant.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    aspirant.position.toLowerCase().includes(searchTerm.toLowerCase()) ||
    aspirant.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const themeStyles = darkMode ? styles.dark : styles.light;

  if (loading) return <p style={styles.loading}>Loading aspirants...</p>;

  return (
    <div style={{ ...styles.container, ...themeStyles.background }}>
      <div style={styles.header}>
        <h1 style={{ ...styles.title, ...themeStyles.title }}>Manage Aspirants</h1>
        <button
          onClick={() => setDarkMode(!darkMode)}
          style={styles.themeBtn}
        >
          {darkMode ? '☀️ Light' : '🌙 Dark'}
        </button>
      </div>

      {error && <p style={styles.error}>{error}</p>}

      <div style={styles.controls}>
        <input
          type="text"
          placeholder="Search by name, position, or email..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ ...styles.searchInput, ...themeStyles.input }}
        />
        <button
          onClick={() => (showForm ? resetForm() : setShowForm(true))}
          style={{
            ...styles.btn,
            backgroundColor: showForm ? '#f97316' : '#16a34a',
          }}
        >
          {showForm ? 'Cancel' : '+ New Aspirant'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} style={{ ...styles.form, ...themeStyles.card }}>
          <h2 style={themeStyles.cardTitle}>
            {editingId ? 'Edit Aspirant' : 'Create New Aspirant'}
          </h2>

          <div style={styles.formGrid}>
            <input
              type="text"
              name="name"
              placeholder="Full Name"
              value={formData.name}
              onChange={handleInputChange}
              required
              style={{ ...styles.input, ...themeStyles.input }}
            />
            <input
              type="email"
              name="email"
              placeholder="Email"
              value={formData.email}
              onChange={handleInputChange}
              required
              style={{ ...styles.input, ...themeStyles.input }}
            />
            <input
              type="tel"
              name="phoneNumber"
              placeholder="Phone Number"
              value={formData.phoneNumber}
              onChange={handleInputChange}
              required
              style={{ ...styles.input, ...themeStyles.input }}
            />
            <select
              name="position"
              value={formData.position}
              onChange={handleInputChange}
              required
              style={{ ...styles.input, ...themeStyles.input }}
            >
              <option value="">Select Position</option>
              {positions.map(pos => (
                <option key={pos} value={pos}>{pos}</option>
              ))}
            </select>

            {/* Conditional Location Fields */}
            {getRequiredLocations(formData.position).includes('county') && (
              <select
                name="countyId"
                value={formData.countyId}
                onChange={handleInputChange}
                required={getRequiredLocations(formData.position).includes('county')}
                style={{ ...styles.input, ...themeStyles.input }}
              >
                <option value="">Select County</option>
                {counties.map(county => (
                  <option key={county.id} value={county.id}>
                    {county.name}
                  </option>
                ))}
              </select>
            )}

            {getRequiredLocations(formData.position).includes('constituency') && (
              <select
                name="constituencyId"
                value={formData.constituencyId}
                onChange={handleInputChange}
                required={getRequiredLocations(formData.position).includes('constituency')}
                style={{ ...styles.input, ...themeStyles.input }}
                disabled={!formData.countyId}
              >
                <option value="">
                  {formData.countyId ? 'Select Constituency' : 'Select County First'}
                </option>
                {constituencies.map(const_item => (
                  <option key={const_item.id} value={const_item.id}>
                    {const_item.name}
                  </option>
                ))}
              </select>
            )}

            {getRequiredLocations(formData.position).includes('ward') && (
              <select
                name="wardId"
                value={formData.wardId}
                onChange={handleInputChange}
                required={getRequiredLocations(formData.position).includes('ward')}
                style={{ ...styles.input, ...themeStyles.input }}
                disabled={!formData.constituencyId}
              >
                <option value="">
                  {formData.constituencyId ? 'Select Ward' : 'Select Constituency First'}
                </option>
                {wards.map(ward => (
                  <option key={ward.id} value={ward.id}>
                    {ward.name}
                  </option>
                ))}
              </select>
            )}
          </div>

          <div style={styles.formGrid}>
            <textarea
              name="bio"
              placeholder="Biography"
              value={formData.bio}
              onChange={handleInputChange}
              style={{ ...styles.textarea, ...themeStyles.input }}
              rows={3}
            />
            <textarea
              name="manifesto"
              placeholder="Manifesto"
              value={formData.manifesto}
              onChange={handleInputChange}
              style={{ ...styles.textarea, ...themeStyles.input }}
              rows={3}
            />
          </div>

          <div style={styles.formActions}>
            <button type="submit" style={styles.submitBtn}>
              {editingId ? 'Update Aspirant' : 'Create Aspirant'}
            </button>
            <button type="button" onClick={resetForm} style={styles.cancelBtn}>
              Cancel
            </button>
          </div>
        </form>
      )}

      <div style={styles.tableLable}>
        <h2 style={themeStyles.cardTitle}>All Aspirants ({filteredAspirants.length})</h2>
      </div>

      {filteredAspirants.length === 0 ? (
        <p style={themeStyles.cardText}>No aspirants found</p>
      ) : (
        <div style={styles.tableWrapper}>
          <table style={styles.table}>
            <thead>
              <tr style={themeStyles.tableHeader}>
                <th style={styles.th}>Name</th>
                <th style={styles.th}>Position</th>
                <th style={styles.th}>Email</th>
                <th style={styles.th}>Phone</th>
                <th style={styles.th}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredAspirants.map(aspirant => (
                <tr key={aspirant.id} style={themeStyles.tableRow}>
                  <td style={styles.td}>{aspirant.name}</td>
                  <td style={styles.td}>
                    <span style={styles.badge}>{aspirant.position}</span>
                  </td>
                  <td style={styles.td}>{aspirant.email}</td>
                  <td style={styles.td}>{aspirant.phoneNumber}</td>
                  <td style={styles.td}>
                    <button
                      onClick={() => handleEdit(aspirant)}
                      style={{ ...styles.actionBtn, backgroundColor: '#3b82f6' }}
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(aspirant.id)}
                      style={{ ...styles.actionBtn, backgroundColor: '#ef4444' }}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <button
        onClick={() => navigate('/admin')}
        style={styles.backBtn}
      >
        ← Back to Admin Dashboard
      </button>
    </div>
  );
};

const styles = {
  container: {
    minHeight: '100vh',
    padding: '24px',
    maxWidth: '1200px',
    margin: '0 auto',
    fontFamily: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
  },
  light: {
    background: { background: 'linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%)', color: '#0f172a' },
    title: { color: '#0f172a' },
    card: { backgroundColor: '#ffffff', border: '1px solid rgba(148, 163, 184, 0.35)' },
    cardTitle: { color: '#0f172a' },
    cardText: { color: '#64748b' },
    input: { backgroundColor: '#fff', color: '#0f172a', borderColor: 'rgba(148, 163, 184, 0.4)' },
    tableHeader: { backgroundColor: '#e2e8f0' },
    tableRow: { borderBottom: '1px solid rgba(148, 163, 184, 0.3)' },
  },
  dark: {
    background: { background: '#0b1220', color: '#f1f5f9' },
    title: { color: '#f1f5f9' },
    card: { backgroundColor: '#0f172a', border: '1px solid rgba(148, 163, 184, 0.18)' },
    cardTitle: { color: '#f1f5f9' },
    cardText: { color: '#cbd5e1' },
    input: { backgroundColor: '#1e293b', color: '#f1f5f9', borderColor: 'rgba(148, 163, 184, 0.2)' },
    tableHeader: { backgroundColor: '#1e293b' },
    tableRow: { borderBottom: '1px solid rgba(148, 163, 184, 0.1)' },
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '32px',
  },
  title: {
    fontSize: '32px',
    fontWeight: '700',
    margin: '0',
  },
  themeBtn: {
    padding: '10px 18px',
    borderRadius: '9999px',
    border: '1px solid rgba(148, 163, 184, 0.4)',
    background: 'rgba(255, 255, 255, 0.9)',
    cursor: 'pointer',
    fontWeight: '600',
  },
  controls: {
    display: 'flex',
    gap: '12px',
    marginBottom: '24px',
  },
  searchInput: {
    flex: 1,
    padding: '12px 16px',
    borderRadius: '8px',
    border: '1px solid rgba(148, 163, 184, 0.4)',
    fontSize: '14px',
  },
  btn: {
    padding: '12px 24px',
    borderRadius: '8px',
    border: 'none',
    color: '#fff',
    fontWeight: '600',
    cursor: 'pointer',
  },
  form: {
    padding: '24px',
    borderRadius: '12px',
    marginBottom: '32px',
    boxShadow: '0 4px 12px rgba(15, 23, 42, 0.08)',
  },
  formGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
    gap: '12px',
    marginBottom: '12px',
  },
  input: {
    padding: '12px 16px',
    borderRadius: '8px',
    border: '1px solid rgba(148, 163, 184, 0.4)',
    fontSize: '14px',
    fontFamily: 'inherit',
  },
  textarea: {
    padding: '12px 16px',
    borderRadius: '8px',
    border: '1px solid rgba(148, 163, 184, 0.4)',
    fontSize: '14px',
    fontFamily: 'inherit',
    resize: 'vertical',
  },
  formActions: {
    display: 'flex',
    gap: '12px',
    marginTop: '16px',
  },
  submitBtn: {
    padding: '12px 24px',
    borderRadius: '8px',
    border: 'none',
    backgroundColor: '#16a34a',
    color: '#fff',
    fontWeight: '600',
    cursor: 'pointer',
  },
  cancelBtn: {
    padding: '12px 24px',
    borderRadius: '8px',
    border: '1px solid rgba(148, 163, 184, 0.4)',
    backgroundColor: 'transparent',
    fontWeight: '600',
    cursor: 'pointer',
  },
  tableLable: {
    marginBottom: '16px',
  },
  tableWrapper: {
    overflowX: 'auto',
    borderRadius: '12px',
    boxShadow: '0 4px 12px rgba(15, 23, 42, 0.08)',
    marginBottom: '32px',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
  },
  th: {
    padding: '12px 16px',
    textAlign: 'left',
    fontWeight: '600',
    fontSize: '14px',
  },
  td: {
    padding: '12px 16px',
    fontSize: '14px',
  },
  badge: {
    display: 'inline-block',
    padding: '4px 12px',
    borderRadius: '9999px',
    backgroundColor: '#dbeafe',
    color: '#0c4a6e',
    fontSize: '12px',
    fontWeight: '600',
  },
  actionBtn: {
    padding: '6px 12px',
    borderRadius: '6px',
    border: 'none',
    color: '#fff',
    fontWeight: '600',
    cursor: 'pointer',
    marginRight: '8px',
    fontSize: '12px',
  },
  backBtn: {
    padding: '12px 24px',
    borderRadius: '9999px',
    border: 'none',
    backgroundColor: '#1d4ed8',
    color: '#fff',
    fontWeight: '700',
    cursor: 'pointer',
  },
  loading: {
    textAlign: 'center',
    padding: '48px',
    fontSize: '18px',
  },
  error: {
    backgroundColor: '#fee2e2',
    color: '#b91c1c',
    padding: '12px 16px',
    borderRadius: '8px',
    marginBottom: '24px',
  },
};

export default AspirantManagement;
