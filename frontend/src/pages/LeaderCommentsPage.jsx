import React, { useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { leadersAPI, commentsAPI, authAPI, pollsAPI } from '../api/apiClient';

// Helper function to format timestamps as relative time
const getRelativeTime = (isoString) => {
  const date = new Date(isoString);
  const now = new Date();
  const diffMs = now - date;
  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffSecs / 60);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffSecs < 60) return 'now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const LeaderCommentsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [leader, setLeader] = useState(null);
  const [comments, setComments] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [commentText, setCommentText] = useState('');
  const [replyTo, setReplyTo] = useState(null);
  const [replyToName, setReplyToName] = useState('');
  const [error, setError] = useState(null);
  const [darkMode, setDarkMode] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const [pollResults, setPollResults] = useState([]);
  const [candidates, setCandidates] = useState([]);
  const [hasVoted, setHasVoted] = useState(false);
  const [pollLoading, setPollLoading] = useState(false);

  const getField = (obj, camelKey, snakeKey) => obj?.[camelKey] ?? obj?.[snakeKey] ?? null;

  const getLeaderLocationText = () => {
    if (!leader) return "";

    const position = leader.position;
    const leaderCounty = getField(leader, "countyName", "county_name");
    const leaderConstituency = getField(leader, "constituencyName", "constituency_name");
    const leaderWard = getField(leader, "wardName", "ward_name");
    const userCounty = getField(currentUser, "countyName", "county_name");
    const userConstituency = getField(currentUser, "constituencyName", "constituency_name");
    const userWard = getField(currentUser, "wardName", "ward_name");

    if (position === "President") {
      return "Kenya";
    }

    if (["Governor", "Senator", "Women Rep"].includes(position)) {
      return userCounty || leaderCounty || "County not available";
    }

    if (position === "MP") {
      return leaderConstituency || userConstituency || leaderCounty || userCounty || "Constituency not available";
    }

    if (position === "MCA") {
      return [userCounty || leaderCounty, userConstituency || leaderConstituency, userWard || leaderWard]
        .filter(Boolean)
        .join(" • ");
    }

    return [leaderCounty, leaderConstituency, leaderWard].filter(Boolean).join(" • ");
  };

  const getVotingScopeText = () => {
    if (!leader) return "Your Location";

    const leaderWard = getField(leader, "wardName", "ward_name");
    const leaderConstituency = getField(leader, "constituencyName", "constituency_name");
    const leaderCounty = getField(leader, "countyName", "county_name");

    if (leaderWard) return `${leaderWard} (Ward)`;
    if (leaderConstituency) return `${leaderConstituency} (Constituency)`;
    if (leaderCounty) return `${leaderCounty} (County)`;
    return "Your Location";
  };

  const loadComments = async (pageToLoad = 0, replace = false) => {
    try {
      setError(null);
      const commentsResp = await commentsAPI.getComments(id, pageToLoad, size);
      const pageData = commentsResp.data || {};
      const pageComments = pageData.comments || [];

      setTotalPages(pageData.totalPages ?? 0);
      setPage(pageData.page ?? pageToLoad);

      setComments((prev) => (replace ? pageComments : [...prev, ...pageComments]));
    } catch (err) {
      console.error(err);
      setError(err?.response?.data || 'Unable to load comments.');
    }
  };

  const loadPollData = async () => {
    if (!leader) return;

    try {
      setPollLoading(true);

      // Load candidates and poll results
      const [candidatesResp, resultsResp, voteCheckResp] = await Promise.all([
        pollsAPI.getCandidates(leader.position, leader.county_id, leader.constituency_id, leader.ward_id),
        pollsAPI.getPollResults(leader.position, leader.county_id, leader.constituency_id, leader.ward_id),
        currentUser ? pollsAPI.checkUserVote(leader.position, leader.county_id, leader.constituency_id, leader.ward_id) : Promise.resolve({ data: { hasVoted: false } })
      ]);

      setCandidates(candidatesResp.data || []);
      setPollResults(resultsResp.data || []);
      setHasVoted(voteCheckResp.data?.hasVoted || false);
    } catch (err) {
      console.error('Error loading poll data:', err);
    } finally {
      setPollLoading(false);
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [leaderResp, userResp] = await Promise.all([
          leadersAPI.getLeader(id),
          authAPI.getCurrentUser().catch(() => null) // Handle case where user is not logged in
        ]);
        
        setLeader(leaderResp.data);
        if (userResp) {
          setCurrentUser(userResp.data);
        }
        
        await loadComments(0, true);
      } catch (err) {
        console.error(err);
        setError(err?.response?.data || 'Unable to load comments.');
      }
    };

    fetchData();
  }, [id]);

  useEffect(() => {
    if (leader) {
      loadPollData();
    }
  }, [leader, currentUser]);

  const submitComment = async () => {
    try {
      setError(null);
      await commentsAPI.createComment(id, commentText, null, replyTo);
      setCommentText('');
      setReplyTo(null);
      setReplyToName('');
      await loadComments(0, true);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data || 'Failed to post comment.');
    }
  };

  const startReply = (commentId, name) => {
    setReplyTo(commentId);
    setReplyToName(name);
  };

  const handleVote = async (candidateId) => {
    if (!currentUser) {
      alert("You must be logged in to vote.");
      return;
    }
    if (hasVoted) return;

    try {
      setPollLoading(true);
      await pollsAPI.castVote(candidateId, leader.position, leader.county_id, leader.constituency_id, leader.ward_id);
      setHasVoted(true);
      await loadPollData(); // Reload poll results
    } catch (err) {
      console.error(err);
      setError("Failed to submit vote.");
    } finally {
      setPollLoading(false);
    }
  };

  const insertEmoji = (emoji) => {
    setCommentText(prev => prev + emoji);
  };

  if (!leader) return <p>Loading...</p>;

  const themeStyles = darkMode ? styles.dark : styles.light;

  return (
    <>
      <style>{`
        .hoverable { transition: transform 140ms ease, box-shadow 140ms ease; }
        .hoverable:hover { transform: translateY(-2px); box-shadow: 0 14px 32px rgba(15, 23, 42, 0.18); }
      `}</style>
      <div style={{ ...styles.container, ...themeStyles.background }}>
        <div style={styles.topBar}>
          <button className="hoverable" onClick={() => navigate('/dashboard')} style={styles.backBtn}>← Back</button>
          <button
            className="hoverable"
            onClick={() => setDarkMode((prev) => !prev)}
            style={styles.themeToggleBtn}
          >
            {darkMode ? 'Light mode' : 'Dark mode'}
          </button>
        </div>

        <h2 style={{ ...styles.header, ...themeStyles.header }}>{leader.name} ({leader.position})</h2>
        {leader.profile_picture && (
          <img src={leader.profile_picture} alt="Leader" style={styles.profileImage} />
        )}
        <p style={themeStyles.subText}>
          <strong>Location:</strong> {getLeaderLocationText() || "Location not available"}
        </p>

        {/* Popularity Poll */}
        <div style={{ ...styles.pollCard, ...themeStyles.card }}>
          <h3 style={styles.pollTitle}>📊 Popularity Poll: {leader.position}</h3>
          
          {/* Location Scope Info */}
          <div style={styles.pollLocationInfo}>
            <p style={styles.pollLocationText}>
              <strong>Voting Scope:</strong> Your vote counts for {getVotingScopeText()}
            </p>
          </div>

          <p style={styles.pollSubtitle}>
            📋 Select your preferred candidate and see live voting results
          </p>

          {pollLoading ? (
            <p>Loading poll data...</p>
          ) : candidates.length === 0 ? (
            <p style={styles.noCandidatesMsg}>
              ⚠️ No candidates available for {leader.position} in your location
            </p>
          ) : (
            <>
              {/* Voting Interface */}
              <div style={styles.votingSection}>
                <h4 style={styles.votingTitle}>👇 How to Vote:</h4>
                {hasVoted ? (
                  <p style={styles.votedMsg}>✅ You've already voted. Thank you for participating!</p>
                ) : !currentUser ? (
                  <p style={styles.votedMsg}>🔒 Please log in to vote.</p>
                ) : (
                  <>
                    <p style={styles.votingInstructions}>
                      Click on a candidate below to cast your vote
                    </p>
                    <div style={styles.candidateButtons}>
                      {candidates.map((candidate) => (
                        <button
                          key={candidate.id}
                          onClick={() => handleVote(candidate.id)}
                          disabled={pollLoading}
                          title={`Vote for ${candidate.name}`}
                          style={{
                            ...styles.candidateBtn,
                            opacity: pollLoading ? 0.5 : 1,
                            cursor: pollLoading ? "not-allowed" : "pointer",
                          }}
                        >
                          {candidate.name} {candidate.party ? `(${candidate.party})` : ''} {candidate.isAspirant ? '(Aspirant)' : '(Incumbent)'}
                        </button>
                      ))}
                    </div>
                  </>
                )}
              </div>

              {/* Poll Results */}
              {pollResults.length > 0 && (
                <div style={styles.pollResults}>
                  <h4 style={styles.resultsTitle}>📈 Current Results:</h4>
                  {pollResults
                    .sort((a, b) => b.percentage - a.percentage)
                    .map((result) => (
                      <div key={result.candidateId} style={styles.resultItem}>
                        <div style={styles.resultBar}>
                          <div
                            style={{
                              ...styles.resultFill,
                              width: `${result.percentage}%`,
                              backgroundColor: result.candidateId === parseInt(id) ? '#3b82f6' : '#6b7280'
                            }}
                          />
                          <span style={styles.resultText}>
                            {result.candidateName} {result.party ? `(${result.party})` : ''} {result.isAspirant ? '(Aspirant)' : '(Incumbent)'}
                            - {result.percentage.toFixed(1)}% ({result.voteCount} {result.voteCount === 1 ? 'vote' : 'votes'})
                          </span>
                        </div>
                      </div>
                    ))}
              </div>
              )}
            </>
          )}
        </div>

      <div style={styles.commentsSection}>
        <h3 style={styles.subHeader}>Public Comments</h3>
        {error && <p style={styles.error}>{error}</p>}

        <div style={styles.commentForm}>
          <textarea
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            placeholder={replyTo ? `Replying to ${replyToName}...` : 'Write a public comment...'}
            style={styles.textarea}
          />
          
          {/* Emoji Buttons */}
          <div style={styles.emojiContainer}>
            <span style={styles.emojiLabel}>Add emoji:</span>
            <div style={styles.emojiButtons}>
              {['👍', '👎', '❤️', '😂', '😢', '😮', '🙌', '🔥', '💯', '👏'].map((emoji) => (
                <button
                  key={emoji}
                  onClick={() => insertEmoji(emoji)}
                  style={styles.emojiBtn}
                  type="button"
                >
                  {emoji}
                </button>
              ))}
            </div>
          </div>
          
          <div style={styles.formRow}>
            <button onClick={submitComment} style={styles.submitBtn} disabled={!commentText.trim()}>
              Post Comment
            </button>
          </div>
          {replyTo && (
            <button onClick={() => { setReplyTo(null); setReplyToName(''); }} style={styles.cancelReply}>
              Cancel reply
            </button>
          )}
        </div>

        {comments.length === 0 ? (
          <p>No comments yet.</p>
        ) : (
          comments.map((comment) => (
            <CommentItem key={comment.id} comment={comment} onReply={startReply} themeStyles={themeStyles} />
          ))
        )}
        {page + 1 < totalPages && (
          <button onClick={() => loadComments(page + 1)} style={styles.loadMoreBtn}>
            Load more comments
          </button>
        )}
      </div>
    </div>
    </>
  );
};

const CommentItem = ({ comment, level = 0, onReply, themeStyles }) => {
  const [collapsed, setCollapsed] = useState(false);
  const replies = comment.replies || [];
  const fullName = `${comment.firstName || ''} ${comment.lastName || ''}`.trim();
  const relativeTime = getRelativeTime(comment.createdAt);

  return (
    <div style={{ ...styles.commentBox, ...themeStyles.commentBox, marginLeft: level > 0 ? '12px' : '0' }} className="hoverable">
      <div style={styles.commentContent}>
        {/* Header: Avatar, Name, Username, Timestamp */}
        <div style={styles.commentHeader}>
          <div style={styles.avatarSection}>
            {comment.photoUrl ? (
              <img src={comment.photoUrl} alt={fullName} style={styles.avatar} />
            ) : (
              <div style={{ ...styles.avatar, ...styles.avatarPlaceholder }}>
                {fullName.charAt(0).toUpperCase()}
              </div>
            )}
          </div>

          <div style={styles.headerContent}>
            <div style={styles.userInfo}>
              <strong style={styles.fullName}>{fullName || 'Anonymous'}</strong>
              <span style={{ ...styles.username, ...themeStyles.username }}>@{comment.username}</span>
              <span style={{ ...styles.timestamp, ...themeStyles.timestamp }}>· {relativeTime}</span>
            </div>
          </div>
        </div>

        {/* Comment Text */}
        <div style={styles.commentTextSection}>
          <p style={{ ...styles.commentText, ...themeStyles.commentText }}>{comment.comment}</p>
        </div>

        {/* Actions: Collapse, Reply */}
        <div style={styles.actions}>
          {replies.length > 0 && (
            <button
              onClick={() => setCollapsed((p) => !p)}
              style={{ ...styles.actionBtn, ...themeStyles.actionBtn }}
            >
              {collapsed ? `↓ ${replies.length} ${replies.length === 1 ? 'reply' : 'replies'}` : `↑ Hide ${replies.length} ${replies.length === 1 ? 'reply' : 'replies'}`}
            </button>
          )}
          <button onClick={() => onReply(comment.id, comment.firstName)} style={{ ...styles.replyBtn, ...themeStyles.replyBtn }}>
            Reply
          </button>
        </div>

        {/* Replies */}
        {!collapsed && replies.length > 0 && (
          <div style={styles.replies}>
            {replies.map((reply) => (
              <CommentItem key={reply.id} comment={reply} level={level + 1} onReply={onReply} themeStyles={themeStyles} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

const styles = {
  container: {
    padding: '16px',
    maxWidth: '600px',
    margin: 'auto',
    fontFamily: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
    minHeight: '100vh',
  },
  light: {
    background: { background: 'linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%)', color: '#0f172a' },
    header: { color: '#0f172a' },
    subText: { color: '#475569' },
    card: { backgroundColor: '#ffffff', border: '1px solid rgba(148, 163, 184, 0.35)' },
    commentBox: { backgroundColor: '#ffffff', border: '1px solid #e2e8f0', borderLeft: '1px solid #cbd5e1' },
    textarea: { background: '#ffffff', color: '#0f172a' },
    username: { color: '#64748b' },
    timestamp: { color: '#94a3b8' },
    commentText: { color: '#0f172a' },
    actionBtn: { color: '#3b82f6' },
    replyBtn: { color: '#3b82f6' },
  },
  dark: {
    background: { background: '#0b1220', color: '#f1f5f9' },
    header: { color: '#f1f5f9' },
    subText: { color: '#cbd5e1' },
    card: { backgroundColor: '#0f172a', border: '1px solid rgba(148, 163, 184, 0.18)' },
    commentBox: { backgroundColor: '#0f172a', border: '1px solid rgba(148, 163, 184, 0.18)', borderLeft: '1px solid rgba(59, 130, 246, 0.3)' },
    textarea: { background: '#0b1220', color: '#f1f5f9', border: '1px solid rgba(148, 163, 184, 0.25)' },
    username: { color: '#94a3b8' },
    timestamp: { color: '#64748b' },
    commentText: { color: '#e2e8f0' },
    actionBtn: { color: '#60a5fa' },
    replyBtn: { color: '#60a5fa' },
  },
  backBtn: {
    marginBottom: '16px',
    cursor: 'pointer',
    backgroundColor: '#1d4ed8',
    padding: '10px 18px',
    borderRadius: '9999px',
    border: 'none',
    color: '#fff',
    fontWeight: '700',
    boxShadow: '0 14px 28px rgba(29, 78, 216, 0.2)',
  },
  header: {
    fontSize: '28px',
    marginBottom: '8px',
    letterSpacing: '0.5px',
  },
  subHeader: {
    fontSize: '20px',
    marginTop: '24px',
    marginBottom: '16px',
    color: '#334155',
  },
  profileImage: {
    width: '120px',
    height: '120px',
    borderRadius: '50%',
    objectFit: 'cover',
    marginBottom: '16px',
    border: '4px solid rgba(59, 130, 246, 0.25)',
  },
  commentsSection: {
    marginTop: '24px',
  },
  commentBox: {
    backgroundColor: '#ffffff',
    padding: '16px',
    borderRadius: '12px',
    marginBottom: '12px',
    border: '1px solid rgba(148, 163, 184, 0.35)',
    boxShadow: '0 2px 8px rgba(15, 23, 42, 0.04)',
    transition: 'transform 140ms ease, box-shadow 140ms ease',
  },
  topBar: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '12px',
  },
  themeToggleBtn: {
    padding: '10px 14px',
    borderRadius: '9999px',
    border: '1px solid rgba(148, 163, 184, 0.4)',
    background: 'rgba(255, 255, 255, 0.22)',
    cursor: 'pointer',
    fontWeight: '700',
    color: '#0f172a',
    boxShadow: '0 10px 22px rgba(15, 23, 42, 0.12)',
  },
  commentContent: {
    width: '100%',
  },
  commentHeader: {
    display: 'flex',
    gap: '12px',
    marginBottom: '12px',
  },
  avatarSection: {
    flexShrink: 0,
  },
  avatar: {
    width: '48px',
    height: '48px',
    borderRadius: '50%',
    objectFit: 'cover',
    border: '2px solid rgba(59, 130, 246, 0.3)',
  },
  avatarPlaceholder: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '20px',
    fontWeight: '700',
    color: '#fff',
    background: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)',
  },
  headerContent: {
    flex: 1,
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: '8px',
  },
  userInfo: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    flexWrap: 'wrap',
  },
  fullName: {
    fontSize: '15px',
    margin: '0',
  },
  username: {
    fontSize: '14px',
    color: '#64748b',
  },
  timestamp: {
    fontSize: '14px',
    color: '#94a3b8',
  },
  commentTextSection: {
    marginLeft: '60px',
    marginBottom: '12px',
  },
  commentText: {
    margin: '0',
    fontSize: '15px',
    lineHeight: '1.5',
    whiteSpace: 'pre-wrap',
    wordWrap: 'break-word',
  },
  actions: {
    display: 'flex',
    gap: '16px',
    marginLeft: '60px',
    marginTop: '10px',
    alignItems: 'center',
  },
  actionBtn: {
    border: 'none',
    background: 'transparent',
    color: '#3b82f6',
    cursor: 'pointer',
    fontSize: '13px',
    fontWeight: '600',
    padding: '4px 0',
    transition: 'color 140ms ease',
  },
  replies: {
    marginLeft: '0',
    marginTop: '12px',
    paddingLeft: '0',
    borderLeft: '2px solid rgba(148, 163, 184, 0.2)',
  },
  commentForm: {
    marginBottom: '28px',
    backgroundColor: '#f8fafc',
    padding: '16px',
    borderRadius: '12px',
  },
  emojiContainer: {
    marginBottom: '12px',
  },
  emojiLabel: {
    fontSize: '14px',
    color: '#475569',
    marginBottom: '8px',
    display: 'block',
  },
  emojiButtons: {
    display: 'flex',
    gap: '6px',
    flexWrap: 'wrap',
  },
  emojiBtn: {
    background: 'transparent',
    border: '1px solid rgba(148, 163, 184, 0.3)',
    borderRadius: '6px',
    padding: '6px 8px',
    cursor: 'pointer',
    fontSize: '16px',
    transition: 'background 140ms ease',
  },
  pollCard: {
    backgroundColor: '#ffffff',
    border: '1px solid rgba(148, 163, 184, 0.35)',
    borderRadius: '16px',
    padding: '20px',
    marginBottom: '24px',
    boxShadow: '0 14px 30px rgba(15, 23, 42, 0.08)',
  },
  pollTitle: {
    margin: '0 0 8px 0',
    fontSize: '18px',
    color: '#0f172a',
  },
  pollSubtitle: {
    margin: '0 0 16px 0',
    fontSize: '14px',
    color: '#64748b',
  },
  pollResults: {
    marginBottom: '20px',
  },
  resultsTitle: {
    margin: '0 0 12px 0',
    fontSize: '16px',
    color: '#0f172a',
    fontWeight: '600',
  },
  resultItem: {
    marginBottom: '8px',
  },
  resultBar: {
    position: 'relative',
    height: '32px',
    backgroundColor: '#f1f5f9',
    borderRadius: '16px',
    overflow: 'hidden',
    display: 'flex',
    alignItems: 'center',
    padding: '0 12px',
  },
  resultFill: {
    position: 'absolute',
    left: 0,
    top: 0,
    height: '100%',
    borderRadius: '16px',
    transition: 'width 0.3s ease',
  },
  resultText: {
    position: 'relative',
    zIndex: 1,
    fontSize: '14px',
    fontWeight: '600',
    color: '#0f172a',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  votingSection: {
    borderTop: '1px solid rgba(148, 163, 184, 0.2)',
    paddingTop: '16px',
  },
  pollLocationInfo: {
    background: 'rgba(59, 130, 246, 0.08)',
    border: '1px solid rgba(59, 130, 246, 0.3)',
    borderRadius: '8px',
    padding: '12px',
    marginBottom: '12px',
  },
  pollLocationText: {
    margin: '0',
    fontSize: '14px',
    color: '#1e40af',
    fontWeight: '500',
  },
  votingInstructions: {
    fontSize: '14px',
    color: '#4b5563',
    marginBottom: '12px',
    padding: '8px 12px',
    backgroundColor: 'rgba(59, 130, 246, 0.05)',
    borderRadius: '6px',
    borderLeft: '3px solid #3b82f6',
  },
  noCandidatesMsg: {
    color: '#ea580c',
    fontSize: '14px',
    padding: '12px',
    backgroundColor: 'rgba(234, 88, 12, 0.08)',
    borderRadius: '8px',
  },
  votingTitle: {
    margin: '0 0 12px 0',
    fontSize: '16px',
    color: '#0f172a',
    fontWeight: '600',
  },
  candidateButtons: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  candidateBtn: {
    padding: '12px 16px',
    border: '1px solid rgba(148, 163, 184, 0.5)',
    borderRadius: '8px',
    background: 'rgba(241, 245, 249, 0.8)',
    cursor: 'pointer',
    fontWeight: '600',
    textAlign: 'left',
    transition: 'transform 120ms ease, background 120ms ease',
  },
  votedMsg: {
    color: '#047857',
    fontWeight: '700',
    margin: '12px 0 0 0',
  },
  textarea: {
    width: '100%',
    minHeight: '100px',
    padding: '12px',
    borderRadius: '12px',
    border: '1px solid rgba(148, 163, 184, 0.6)',
    resize: 'vertical',
    fontSize: '15px',
    outline: 'none',
    boxShadow: 'inset 0 2px 6px rgba(15, 23, 42, 0.05)',
    marginBottom: '12px',
    fontFamily: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
  },
  formRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    flexWrap: 'wrap',
    gap: '12px',
  },
  select: {
    marginLeft: '8px',
    padding: '8px 10px',
    borderRadius: '12px',
    border: '1px solid rgba(148, 163, 184, 0.6)',
    background: 'white',
  },
  submitBtn: {
    padding: '10px 20px',
    backgroundColor: '#0d9488',
    color: 'white',
    border: 'none',
    borderRadius: '9999px',
    cursor: 'pointer',
    fontWeight: '700',
    fontSize: '14px',
    boxShadow: '0 10px 22px rgba(13, 148, 136, 0.25)',
    transition: 'transform 140ms ease',
  },
  cancelReply: {
    marginTop: '10px',
    background: 'transparent',
    border: 'none',
    color: '#334155',
    cursor: 'pointer',
    textDecoration: 'underline',
    fontWeight: '600',
    fontSize: '14px',
  },
  error: {
    color: '#b91c1c',
    marginBottom: '16px',
    padding: '12px',
    backgroundColor: '#fee2e2',
    borderRadius: '8px',
  },
  replyBtn: {
    border: 'none',
    background: 'transparent',
    color: '#3b82f6',
    cursor: 'pointer',
    fontSize: '13px',
    fontWeight: '600',
    padding: '4px 0',
    transition: 'color 140ms ease',
  },
  loadMoreBtn: {
    marginTop: '20px',
    padding: '12px 18px',
    backgroundColor: '#1d4ed8',
    color: '#fff',
    border: 'none',
    borderRadius: '9999px',
    cursor: 'pointer',
    fontWeight: '700',
    boxShadow: '0 10px 22px rgba(29, 78, 216, 0.25)',
    width: '100%',
  },
};

export default LeaderCommentsPage;
