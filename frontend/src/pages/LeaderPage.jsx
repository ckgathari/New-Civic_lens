import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { leadersAPI, authAPI, commentsAPI } from "../api/apiClient";

export default function LeaderPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [leader, setLeader] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [avgRating, setAvgRating] = useState(0);
  const [userHasReviewed, setUserHasReviewed] = useState(false);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      setLoading(true);
      try {
        const { data: user } = await authAPI.getCurrentUser();
        if (!cancelled) setCurrentUser(user);

        const leaderResp = await leadersAPI.getLeader(id);
        if (!cancelled) setLeader(leaderResp.data);

        const commentsResp = await commentsAPI.getComments(id);
        const leaderComments = commentsResp.data?.comments || [];
        if (!cancelled) {
          setComments(leaderComments);

          const ratedComments = leaderComments.filter((c) => typeof c.rating === 'number');
          if (ratedComments.length > 0) {
            const avg = ratedComments.reduce((s, c) => s + c.rating, 0) / ratedComments.length;
            setAvgRating(Number(avg.toFixed(1)));
          }

          if (user) {
            setUserHasReviewed(
              leaderComments.some((c) => c.userId === user.id && typeof c.rating === 'number')
            );
          }
        }
      } catch (err) {
        console.error("Error loading leader page:", err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => { cancelled = true; };
  }, [id]);

  const handleRate = async (stars) => {
    if (!currentUser) {
      alert("You must be logged in to rate.");
      return;
    }
    if (userHasReviewed) return;
    try {
      await commentsAPI.createComment(id, "", stars, null);
      const resp = await commentsAPI.getComments(id);
      const leaderComments = resp.data?.comments || [];
      setComments(leaderComments);
      const ratedComments = leaderComments.filter((c) => typeof c.rating === 'number');
      if (ratedComments.length > 0) {
        const avg = ratedComments.reduce((s, c) => s + c.rating, 0) / ratedComments.length;
        setAvgRating(Number(avg.toFixed(1)));
      }
      setUserHasReviewed(true);
    } catch (err) {
      console.error(err);
    }
  };

  const postComment = async () => {
    if (!currentUser) {
      alert("You must be logged in to comment.");
      return;
    }
    if (!newComment.trim()) return;
    try {
      await commentsAPI.createComment(id, newComment.trim(), null, null);
      setNewComment("");
      const resp = await commentsAPI.getComments(id);
      setComments(resp.data?.comments || []);
    } catch (err) {
      console.error(err);
    }
  };

  const getField = (obj, camelKey, snakeKey) => obj?.[camelKey] ?? obj?.[snakeKey] ?? null;

  const getLeaderLocationText = () => {
    if (!leader) return "";

    const position = leader.position;
    const leaderCounty = getField(leader, "countyName", "county_name");
    const leaderConstituency = getField(leader, "constituencyName", "constituency_name");
    const leaderWard = getField(leader, "wardName", "ward_name");
    const currentCounty = getField(currentUser, "countyName", "county_name");
    const currentConstituency = getField(currentUser, "constituencyName", "constituency_name");
    const currentWard = getField(currentUser, "wardName", "ward_name");

    if (position === "President") {
      return "Kenya";
    }

    if (["Governor", "Senator", "Women Rep"].includes(position)) {
      return currentCounty || leaderCounty || "County not available";
    }

    if (position === "MP") {
      return leaderConstituency || currentConstituency || leaderCounty || currentCounty || "Constituency not available";
    }

    if (position === "MCA") {
      return [currentCounty || leaderCounty, currentConstituency || leaderConstituency, currentWard || leaderWard]
        .filter(Boolean)
        .join(" • ");
    }

    return [leaderCounty, leaderConstituency, leaderWard].filter(Boolean).join(" • ");
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 px-4 py-16 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl rounded-3xl border border-slate-200 bg-white px-8 py-10 shadow-sm">
          <div className="inline-flex items-center gap-3">
            <div className="h-3 w-3 animate-pulse rounded-full bg-sky-500" />
            <p className="text-sm font-medium text-slate-700">Loading leader profile...</p>
          </div>
        </div>
      </div>
    );
  }

  const locationText = getLeaderLocationText();

  return (
    <div className="min-h-screen bg-slate-50 py-8">
      <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.28em] text-sky-600">Leader engagement</p>
            <h1 className="mt-3 text-3xl font-semibold tracking-tight text-slate-900 sm:text-4xl">
              {leader?.name}
            </h1>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-slate-600">
              Explore public feedback, ratings, and comments for this leader in your community.
            </p>
          </div>
          <button
            onClick={() => navigate("/dashboard")}
            className="inline-flex items-center justify-center rounded-full border border-slate-200 bg-white px-5 py-2 text-sm font-semibold text-slate-900 transition hover:border-slate-300 hover:bg-slate-50"
          >
            ← Back to dashboard
          </button>
        </div>

        <div className="mt-8 grid gap-6 xl:grid-cols-[1fr_1.4fr]">
          <aside className="space-y-6 rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
            <div className="rounded-[1.75rem] border border-slate-200 bg-slate-50 p-6">
              <p className="text-sm font-semibold uppercase tracking-[0.28em] text-slate-700">Profile</p>
              <div className="mt-5 space-y-4">
                <div>
                  <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Name</p>
                  <p className="mt-2 text-base font-semibold text-slate-900">{leader?.name}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Position</p>
                  <p className="mt-2 text-base font-semibold text-slate-900">{leader?.position}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Location</p>
                  <p className="mt-2 text-base font-semibold text-slate-900">{locationText || "Location not available"}</p>
                </div>
                <div className="rounded-3xl bg-white p-4 shadow-sm">
                  <p className="text-xs uppercase tracking-[0.3em] text-slate-500">Average rating</p>
                  <p className="mt-3 text-3xl font-semibold text-slate-900">{avgRating || "—"}</p>
                </div>
              </div>
            </div>

            <div className="rounded-[1.75rem] border border-slate-200 bg-slate-50 p-6">
              <p className="text-sm font-semibold uppercase tracking-[0.28em] text-slate-700">Action</p>
              <p className="mt-3 text-sm leading-6 text-slate-600">
                Rate this leader and follow the discussion thread to stay engaged.
              </p>
              <div className="mt-6 flex flex-col gap-3">
                <button
                  type="button"
                  onClick={() => navigate(`/leader-comments/${id}`)}
                  className="rounded-3xl bg-slate-900 px-5 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
                >
                  Open discussion page
                </button>
                <button
                  type="button"
                  onClick={() => navigate("/dashboard")}
                  className="rounded-3xl border border-slate-200 bg-white px-5 py-3 text-sm font-semibold text-slate-900 transition hover:border-slate-300 hover:bg-slate-50"
                >
                  Browse other leaders
                </button>
              </div>
            </div>
          </aside>

          <main className="space-y-6">
            <section className="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h2 className="text-xl font-semibold text-slate-900">Rate this leader</h2>
                  <p className="mt-2 text-sm leading-6 text-slate-600">
                    Share your review to help your community track local leader performance.
                  </p>
                </div>
                <span className="rounded-full bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700">
                  {userHasReviewed ? "Already reviewed" : "Open rating"}
                </span>
              </div>

              <div className="mt-6 grid gap-3 sm:grid-cols-5">
                {[1, 2, 3, 4, 5].map((n) => (
                  <button
                    type="button"
                    key={n}
                    onClick={() => handleRate(n)}
                    disabled={userHasReviewed}
                    className={`rounded-full border px-4 py-3 text-sm font-semibold transition ${
                      userHasReviewed
                        ? "cursor-not-allowed border-slate-200 bg-slate-100 text-slate-400"
                        : "border-slate-200 bg-slate-50 text-slate-900 hover:border-sky-300 hover:bg-sky-50"
                    }`}
                  >
                    {n} ⭐
                  </button>
                ))}
              </div>

              {userHasReviewed && (
                <div className="mt-4 rounded-3xl bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700">
                  Thanks for sharing your rating — your feedback is now visible to your community.
                </div>
              )}
            </section>

            <section className="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
              <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h2 className="text-xl font-semibold text-slate-900">Public discussion</h2>
                  <p className="mt-2 text-sm leading-6 text-slate-600">
                    Add a quick comment or open the threaded discussion for more context.
                  </p>
                </div>
                <span className="rounded-full bg-sky-50 px-4 py-2 text-sm font-semibold text-sky-700">
                  {comments.length} comments
                </span>
              </div>

              <textarea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                placeholder="Share your thoughts..."
                className="mt-6 min-h-[140px] w-full rounded-3xl border border-slate-200 bg-slate-50 px-5 py-4 text-sm text-slate-900 outline-none transition focus:border-sky-300 focus:ring-4 focus:ring-sky-100"
              />

              <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <button
                  type="button"
                  onClick={postComment}
                  disabled={!newComment.trim()}
                  className="inline-flex items-center justify-center rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:bg-slate-300"
                >
                  Post comment
                </button>
                <button
                  type="button"
                  onClick={() => navigate(`/leader-comments/${id}`)}
                  className="inline-flex items-center justify-center rounded-full border border-slate-200 bg-white px-6 py-3 text-sm font-semibold text-slate-900 transition hover:border-slate-300 hover:bg-slate-50"
                >
                  Open threaded discussion
                </button>
              </div>
            </section>

            <section className="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm">
              <div className="space-y-4">
                <div>
                  <h2 className="text-xl font-semibold text-slate-900">Latest comments</h2>
                  <p className="mt-2 text-sm leading-6 text-slate-600">
                    Recent leader feedback from your area.
                  </p>
                </div>

                {comments.length === 0 ? (
                  <div className="rounded-3xl border border-dashed border-slate-200 bg-slate-50 p-6 text-sm text-slate-600">
                    No comments yet. Be the first to leave feedback.
                  </div>
                ) : (
                  <div className="space-y-4">
                    {comments.map((comment) => (
                      <article key={comment.id} className="rounded-3xl border border-slate-200 bg-slate-50 p-5 transition hover:border-slate-300">
                        <p className="text-sm leading-7 text-slate-800">{comment.comment || "(No comment text)"}</p>
                        <div className="mt-4 flex flex-wrap items-center gap-3 text-xs text-slate-500">
                          <span>{comment.username || comment.userName || "Anonymous"}</span>
                          <span className="h-1.5 w-1.5 rounded-full bg-slate-400" />
                          <span>{new Date(comment.created_at).toLocaleDateString()}</span>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </div>
            </section>
          </main>
        </div>
      </div>
    </div>
  );
}

