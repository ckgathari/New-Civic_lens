import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminAPI } from '../api/apiClient';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [darkMode, setDarkMode] = useState(false);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      setLoading(true);
      const response = await adminAPI.getAdminStats();
      setStats(response.data);
      setError(null);
    } catch (err) {
      console.error('Error loading stats:', err);
      setError('Failed to load admin dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const themeStyles = darkMode ? styles.dark : styles.light;

  if (loading) return <p style={styles.loading}>Loading dashboard...</p>;

  return (
    <div style={{ ...styles.container, ...themeStyles.background }}>
      <style>{`
        .admin-card { transition: transform 140ms ease, box-shadow 140ms ease; }
        .admin-card:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(15, 23, 42, 0.15); }
      `}</style>

      <div style={styles.header}>
        <h1 style={{ ...styles.title, ...themeStyles.title }}>Admin Dashboard</h1>
        <button
          onClick={() => setDarkMode(!darkMode)}
          style={styles.themeBtn}
        >
          {darkMode ? '☀️ Light' : '🌙 Dark'}
        </button>
      </div>

      {error && <p style={styles.error}>{error}</p>}

      <div style={styles.statsGrid}>
        <div className="admin-card" style={{ ...styles.statCard, ...themeStyles.card }}>
          <h3 style={themeStyles.cardTitle}>Total Comments</h3>
          <p style={styles.statNumber}>{stats?.totalComments || 0}</p>
        </div>
        <div className="admin-card" style={{ ...styles.statCard, ...themeStyles.card }}>
          <h3 style={themeStyles.cardTitle}>Visible Comments</h3>
          <p style={{ ...styles.statNumber, color: '#059669' }}>{stats?.visibleComments || 0}</p>
        </div>
        <div className="admin-card" style={{ ...styles.statCard, ...themeStyles.card }}>
          <h3 style={themeStyles.cardTitle}>Hidden Comments</h3>
          <p style={{ ...styles.statNumber, color: '#dc2626' }}>{stats?.hiddenComments || 0}</p>
        </div>
      </div>

      <div style={styles.navigationGrid}>
        <button
          onClick={() => navigate('/admin/aspirants')}
          className="admin-card"
          style={{ ...styles.navCard, ...themeStyles.card }}
        >
          <h3 style={themeStyles.cardTitle}>Manage Aspirants</h3>
          <p style={themeStyles.cardText}>Create, edit, and delete aspirants</p>
        </button>

        <button
          onClick={() => navigate('/admin/comments')}
          className="admin-card"
          style={{ ...styles.navCard, ...themeStyles.card }}
        >
          <h3 style={themeStyles.cardTitle}>Moderate Comments</h3>
          <p style={themeStyles.cardText}>Review and moderate user comments</p>
        </button>
      </div>

      <button
        onClick={() => navigate('/dashboard')}
        style={styles.backBtn}
      >
        ← Back to Dashboard
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
    cardTitle: { color: '#0f172a', margin: '0 0 8px 0' },
    cardText: { color: '#64748b', margin: '0' },
  },
  dark: {
    background: { background: '#0b1220', color: '#f1f5f9' },
    title: { color: '#f1f5f9' },
    card: { backgroundColor: '#0f172a', border: '1px solid rgba(148, 163, 184, 0.18)' },
    cardTitle: { color: '#f1f5f9', margin: '0 0 8px 0' },
    cardText: { color: '#cbd5e1', margin: '0' },
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
    border:'1px solid rgba(148, 163, 184, 0.4)',
    background: 'rgba(255, 255, 255, 0.9)',
    cursor: 'pointer',
    fontWeight: '600',
  },
  statsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
    gap: '20px',
    marginBottom: '40px',
  },
  statCard: {
    padding: '24px',
    borderRadius: '12px',
    boxShadow: '0 4px 12px rgba(15, 23, 42, 0.08)',
  },
  statNumber: {
    fontSize: '36px',
    fontWeight: '700',
    margin: '12px 0 0 0',
    color: '#3b82f6',
  },
  navigationGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
    gap: '24px',
    marginBottom: '32px',
  },
  navCard: {
    padding: '32px',
    borderRadius: '12px',
    cursor: 'pointer',
    textAlign: 'left',
    border: 'none',
    boxShadow: '0 4px 12px rgba(15, 23, 42, 0.08)',
  },
  backBtn: {
    padding: '12px 24px',
    borderRadius: '9999px',
    border: 'none',
    backgroundColor: '#1d4ed8',
    color: '#fff',
    fontWeight: '700',
    cursor: 'pointer',
    marginTop: '24px',
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

export default AdminDashboard;
