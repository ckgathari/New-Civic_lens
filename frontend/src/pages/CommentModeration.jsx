import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminAPI } from '../api/apiClient';

const CommentModeration = () => {
  const navigate = useNavigate();
  const [comments, setComments] = useState([]);
  const [flaggedComments, setFlaggedComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('all');
  const [darkMode, setDarkMode] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedId, setExpandedId] = useState(null);

  useEffect(() => {
    loadComments();
  }, []);

  const loadComments = async () => {
    try {
      setLoading(true);
      const [allResponse, flaggedResponse] = await Promise.all([
        adminAPI.getAllComments(),
        adminAPI.getFlaggedComments(),
      ]);
      setComments(allResponse.data);
      setFlaggedComments(flaggedResponse.data);
      setError(null);
    } catch (err) {
      console.error('Error loading comments:', err);
      setError('Failed to load comments');
    } finally {
      setLoading(false);
    }
  };

  const handleHideComment = async (id) => {
    try {
      await adminAPI.hideComment(id);
      loadComments();
      setError(null);
    } catch (err) {
      console.error('Error hiding comment:', err);
      setError('Failed to hide comment');
    }
  };

  const handleUnhideComment = async (id) => {
    try {
      await adminAPI.unhideComment(id);
      loadComments();
      setError(null);
    } catch (err) {
      console.error('Error unhiding comment:', err);
      setError('Failed to unhide comment');
    }
  };

  const handleDeleteComment = async (id) => {
    if (window.confirm('Are you sure you want to permanently delete this comment?')) {
      try {
        await adminAPI.deleteComment(id);
        loadComments();
        setError(null);
      } catch (err) {
        console.error('Error deleting comment:', err);
        setError('Failed to delete comment');
      }
    }
  };

  const filteredComments = (activeTab === 'flagged' ? flaggedComments : comments).filter(comment =>
    comment.content.toLowerCase().includes(searchTerm.toLowerCase()) ||
    comment.authorName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const themeStyles = darkMode ? styles.dark : styles.light;

  if (loading) return <p style={styles.loading}>Loading comments...</p>;

  const CommentCard = ({ comment }) => {
    const isExpanded = expandedId === comment.id;
    const contentPreview = comment.content.substring(0, 150);
    const isLongContent = comment.content.length > 150;

    return (
      <div
        key={comment.id}
        style={{ ...styles.commentCard, ...themeStyles.card }}
      >
        <div style={styles.commentHeader}>
          <div>
            <p style={{ ...styles.authorName, ...themeStyles.cardTitle }}>
              {comment.authorName}
            </p>
            <p style={{ ...styles.timestamp, ...themeStyles.cardText }}>
              {new Date(comment.createdAt).toLocaleString()}
            </p>
          </div>
          <div style={styles.badges}>
            {comment.isHidden && (
              <span style={styles.hiddenBadge}>Hidden</span>
            )}
          </div>
        </div>

        <p style={{ ...styles.commentContent, ...themeStyles.cardText }}>
          {isExpanded ? comment.content : contentPreview}
          {isLongContent && !isExpanded && '...'}
        </p>

        {isLongContent && (
          <button
            onClick={() => setExpandedId(isExpanded ? null : comment.id)}
            style={styles.expandBtn}
          >
            {isExpanded ? 'Show less' : 'Show more'}
          </button>
        )}

        <div style={styles.actions}>
          {comment.isHidden ? (
            <button
              onClick={() => handleUnhideComment(comment.id)}
              style={{ ...styles.actionBtn, backgroundColor: '#16a34a' }}
            >
              ✓ Unhide
            </button>
          ) : (
            <button
              onClick={() => handleHideComment(comment.id)}
              style={{ ...styles.actionBtn, backgroundColor: '#f59e0b' }}
            >
              👁‍🗨 Hide
            </button>
          )}
          <button
            onClick={() => handleDeleteComment(comment.id)}
            style={{ ...styles.actionBtn, backgroundColor: '#ef4444' }}
          >
            🗑 Delete
          </button>
        </div>
      </div>
    );
  };

  return (
    <div style={{ ...styles.container, ...themeStyles.background }}>
      <div style={styles.header}>
        <h1 style={{ ...styles.title, ...themeStyles.title }}>Comment Moderation</h1>
        <button
          onClick={() => setDarkMode(!darkMode)}
          style={styles.themeBtn}
        >
          {darkMode ? '☀️ Light' : '🌙 Dark'}
        </button>
      </div>

      {error && <p style={styles.error}>{error}</p>}

      <div style={styles.controls}>
        <div style={styles.tabs}>
          <button
            onClick={() => setActiveTab('all')}
            style={{
              ...styles.tab,
              ...themeStyles.tab,
              ...(activeTab === 'all' ? styles.activeTab : {}),
            }}
          >
            All Comments ({comments.length})
          </button>
          <button
            onClick={() => setActiveTab('flagged')}
            style={{
              ...styles.tab,
              ...themeStyles.tab,
              ...(activeTab === 'flagged' ? styles.activeTab : {}),
            }}
          >
            Hidden ({flaggedComments.length})
          </button>
        </div>

        <input
          type="text"
          placeholder="Search comments or author..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ ...styles.searchInput, ...themeStyles.input }}
        />
      </div>

      {filteredComments.length === 0 ? (
        <p style={{ ...styles.noData, ...themeStyles.cardText }}>
          {activeTab === 'flagged'
            ? 'No hidden comments'
            : 'No comments found'}
        </p>
      ) : (
        <div style={styles.commentsList}>
          {filteredComments.map(comment => (
            <CommentCard key={comment.id} comment={comment} />
          ))}
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
    tab: { color: '#64748b', borderBottomColor: 'rgba(148, 163, 184, 0.2)' },
  },
  dark: {
    background: { background: '#0b1220', color: '#f1f5f9' },
    title: { color: '#f1f5f9' },
    card: { backgroundColor: '#0f172a', border: '1px solid rgba(148, 163, 184, 0.18)' },
    cardTitle: { color: '#f1f5f9' },
    cardText: { color: '#cbd5e1' },
    input: { backgroundColor: '#1e293b', color: '#f1f5f9', borderColor: 'rgba(148, 163, 184, 0.2)' },
    tab: { color: '#cbd5e1', borderBottomColor: 'rgba(148, 163, 184, 0.1)' },
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
    marginBottom: '32px',
  },
  tabs: {
    display: 'flex',
    gap: '8px',
    marginBottom: '16px',
  },
  tab: {
    padding: '12px 24px',
    border: 'none',
    borderBottom: '3px solid transparent',
    background: 'transparent',
    fontWeight: '600',
    cursor: 'pointer',
    fontSize: '14px',
  },
  activeTab: {
    borderBottomColor: '#3b82f6',
    color: '#3b82f6',
  },
  searchInput: {
    width: '100%',
    maxWidth: '500px',
    padding: '12px 16px',
    borderRadius: '8px',
    border: '1px solid rgba(148, 163, 184, 0.4)',
    fontSize: '14px',
  },
  commentsList: {
    display: 'grid',
    gap: '16px',
    marginBottom: '32px',
  },
  commentCard: {
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 4px 12px rgba(15, 23, 42, 0.08)',
  },
  commentHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '12px',
  },
  authorName: {
    fontSize: '16px',
    fontWeight: '700',
    margin: '0 0 4px 0',
  },
  timestamp: {
    fontSize: '13px',
    margin: '0',
    opacity: 0.7,
  },
  badges: {
    display: 'flex',
    gap: '8px',
  },
  hiddenBadge: {
    display: 'inline-block',
    padding: '4px 12px',
    borderRadius: '9999px',
    backgroundColor: '#fee2e2',
    color: '#b91c1c',
    fontSize: '12px',
    fontWeight: '600',
  },
  commentContent: {
    margin: '12px 0',
    lineHeight: '1.6',
  },
  expandBtn: {
    padding: '6px 12px',
    borderRadius: '6px',
    border: 'none',
    background: 'rgba(59, 130, 246, 0.1)',
    color: '#3b82f6',
    fontWeight: '600',
    cursor: 'pointer',
    fontSize: '12px',
    marginBottom: '12px',
  },
  actions: {
    display: 'flex',
    gap: '8px',
    marginTop: '12px',
  },
  actionBtn: {
    padding: '8px 16px',
    borderRadius: '6px',
    border: 'none',
    color: '#fff',
    fontWeight: '600',
    cursor: 'pointer',
    fontSize: '13px',
  },
  noData: {
    textAlign: 'center',
    padding: '48px 24px',
    fontSize: '16px',
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

export default CommentModeration;
