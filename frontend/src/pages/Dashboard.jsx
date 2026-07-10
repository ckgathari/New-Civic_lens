import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authAPI, leadersAPI, commentsAPI, pollsAPI } from "../api/apiClient";

const positionOrder = ["President", "Governor", "Senator", "Women Rep", "MP", "MCA"];

const Dashboard = () => {
  const [leaders, setLeaders] = useState([]);
  const [leaderRatings, setLeaderRatings] = useState([]);
  const [positionPolls, setPositionPolls] = useState({});
  const [userVotes, setUserVotes] = useState({});
  const [currentUser, setCurrentUser] = useState(null);
  const [locationLabel, setLocationLabel] = useState("");
  const [loading, setLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);
  const navigate = useNavigate();

  const getField = (obj, camel, snake) => obj?.[camel] ?? obj?.[snake] ?? "";

  const handleLogout = async () => {
    try {
      await authAPI.logout();
    } catch (err) {
      console.warn("Logout request failed, clearing local state anyway:", err);
    }

    localStorage.removeItem("authToken");
    localStorage.removeItem("userId");
    navigate("/login");
  };

  const computeAverageRating = (comments = []) => {
    const ratedComments = comments.filter((c) => typeof c.rating === "number");
    if (!ratedComments.length) return { avgRating: 0, ratingCount: 0 };
    const total = ratedComments.reduce((sum, c) => sum + c.rating, 0);
    return {
      avgRating: Number((total / ratedComments.length).toFixed(1)),
      ratingCount: ratedComments.length,
    };
  };

  const buildLocationLabel = (user) => {
    const county = getField(user, "countyName", "county_name");
    const constituency = getField(user, "constituencyName", "constituency_name");
    const ward = getField(user, "wardName", "ward_name");
    const parts = [county, constituency, ward].filter(Boolean);
    return parts.length ? parts.join(" • ") : "Location not set";
  };

  const fetchPositionPollData = async (position, countyId, constituencyId, wardId) => {
    const [candidatesResp, resultsResp, userVoteResp] = await Promise.all([
      pollsAPI.getCandidates(position, countyId, constituencyId, wardId),
      pollsAPI.getPollResults(position, countyId, constituencyId, wardId),
      pollsAPI.getUserVote(position, countyId, constituencyId, wardId).catch(() => ({ data: { hasVoted: false } }))
    ]);

    return {
      candidates: candidatesResp?.data || [],
      results: resultsResp?.data || [],
      userVote: userVoteResp?.data || { hasVoted: false }
    };
  };

  const handleVoteForPosition = async (position, candidate) => {
    if (!currentUser) {
      alert("Please log in to vote.");
      return;
    }

    if (userVotes[position]) {
      alert("You have already voted for this position.");
      return;
    }

    try {
      await pollsAPI.castVote(candidate.id, candidate.type, position, currentUser.countyId || currentUser.county_id, currentUser.constituencyId || currentUser.constituency_id, currentUser.wardId || currentUser.ward_id);
      const updatedPoll = await fetchPositionPollData(position, currentUser.countyId || currentUser.county_id, currentUser.constituencyId || currentUser.constituency_id, currentUser.wardId || currentUser.ward_id);
      setPositionPolls((prev) => ({ ...prev, [position]: updatedPoll }));
      setUserVotes((prev) => ({ ...prev, [position]: candidate.id }));
    } catch (error) {
      console.error("Vote failed", error);
      alert("Failed to submit your vote. Please try again.");
    }
  };

  const getUniqueLeaders = (list) => {
    const map = new Map();
    list.forEach((leader) => {
      if (!map.has(leader.id)) {
        map.set(leader.id, leader);
      }
    });
    return Array.from(map.values());
  };

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);

        const tokenCheck = await authAPI.checkToken();
        if (!tokenCheck.data.valid) {
          navigate("/login");
          return;
        }

        const { data: user } = await authAPI.getCurrentUser();
        if (!user?.id) {
          navigate("/login");
          return;
        }

        setCurrentUser(user);
        setLocationLabel(buildLocationLabel(user));
        setIsAdmin(false);

        const countyId = getField(user, "countyId", "county_id");
        const constituencyId = getField(user, "constituencyId", "constituency_id");
        const wardId = getField(user, "wardId", "ward_id");

        if (!countyId && !constituencyId && !wardId) {
          setLeaders([]);
          return;
        }

        const allLeaders = [];
        const presidentResp = await leadersAPI.getLeadersByPosition("President");
        if (presidentResp?.data) {
          allLeaders.push(...presidentResp.data);
        }

        if (countyId) {
          const countyResp = await leadersAPI.getLeadersByCounty(Number(countyId));
          if (countyResp?.data) allLeaders.push(...countyResp.data);
        }

        if (constituencyId) {
          const mpResp = await leadersAPI.getLeadersByPosition("MP");
          if (mpResp?.data) {
            allLeaders.push(
              ...mpResp.data.filter((leader) => String(getField(leader, "constituencyId", "constituency_id")) === String(constituencyId))
            );
          }
        }

        if (wardId) {
          const mcaResp = await leadersAPI.getLeadersByPosition("MCA");
          if (mcaResp?.data) {
            allLeaders.push(
              ...mcaResp.data.filter((leader) => String(getField(leader, "wardId", "ward_id")) === String(wardId))
            );
          }
        }

        const uniqueLeaders = getUniqueLeaders(allLeaders);
        const sortedLeaders = uniqueLeaders.sort((a, b) => {
          const aIndex = positionOrder.indexOf(a.position);
          const bIndex = positionOrder.indexOf(b.position);
          return aIndex - bIndex;
        });

        setLeaders(sortedLeaders);

        const ratingsData = await Promise.all(
          sortedLeaders.map(async (leader) => {
            const commentResp = await commentsAPI.getComments(leader.id, 0, 60);
            const comments = commentResp.data?.comments || [];
            const { avgRating, ratingCount } = computeAverageRating(comments);
            return {
              id: leader.id,
              name: leader.name,
              position: leader.position,
              avgRating,
              ratingCount,
            };
          })
        );

        setLeaderRatings(
          ratingsData.sort((a, b) => b.avgRating - a.avgRating || b.ratingCount - a.ratingCount)
        );

        const pollData = {};
        const voteState = {};
        await Promise.all(
          positionOrder.map(async (position) => {
            const data = await fetchPositionPollData(
              position,
              Number(countyId),
              constituencyId ? Number(constituencyId) : undefined,
              wardId ? Number(wardId) : undefined
            );
            pollData[position] = data;
            voteState[position] = data.userVote?.candidateId || null;
          })
        );
        setPositionPolls(pollData);
        setUserVotes(voteState);
      } catch (error) {
        console.error("Error loading dashboard data:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [navigate]);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 px-6 py-24 text-center text-slate-600">
        <div className="inline-flex items-center justify-center gap-3 rounded-3xl border border-slate-200 bg-white px-8 py-6 shadow-sm">
          <div className="h-3 w-3 animate-pulse rounded-full bg-sky-500" />
          <p className="text-sm font-medium">Loading leaders and location insights…</p>
        </div>
      </div>
    );
  }

  if (!leaders.length) {
    return (
      <div className="min-h-screen bg-slate-50 px-4 py-16 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-3xl rounded-[2rem] border border-slate-200 bg-white p-10 text-center shadow-sm">
          <p className="text-sm font-semibold uppercase tracking-[0.24em] text-sky-600">Location required</p>
          <h1 className="mt-4 text-3xl font-semibold text-slate-900">Pick your area to see local leaders</h1>
          <p className="mt-3 text-sm leading-6 text-slate-600">
            Your dashboard needs your county, constituency, or ward to display regional leader ratings and engagement links.
          </p>
          <Link
            to="/location-selector"
            className="mt-8 inline-flex rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
          >
            Select your location
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 backdrop-blur-md">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.24em] text-sky-600">CivicLens</p>
            <h1 className="text-2xl font-semibold text-slate-900 sm:text-3xl">Local leader engagement</h1>
          </div>
          <div className="flex items-center gap-3">
            {isAdmin && (
              <Link
                to="/admin"
                className="rounded-full bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800"
              >
                Admin dashboard
              </Link>
            )}
            <button
              onClick={handleLogout}
              className="rounded-full border border-slate-200 bg-white px-5 py-2 text-sm font-semibold text-slate-900 transition hover:border-slate-300 hover:bg-slate-50"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-5 sm:px-6 lg:px-8">
        <div className="grid gap-4 lg:grid-cols-[260px_minmax(0,1fr)]">
          <aside className="space-y-4 rounded-[1.5rem] border border-slate-200 bg-white p-4 shadow-sm">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.24em] text-sky-600">Leaders</p>
              <h2 className="mt-2 text-lg font-semibold text-slate-900">Your local roster</h2>
              <p className="mt-1 text-sm leading-5 text-slate-600">
                Tap a leader name to view their public engagement page and join the conversation.
              </p>
            </div>
            <div className="space-y-1.5">
              {leaders.map((leader) => (
                <Link
                  key={leader.id}
                  to={`/leader/${leader.id}`}
                  className="block rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2.5 transition hover:border-sky-300 hover:bg-slate-100"
                >
                  <div className="flex items-center justify-between gap-4">
                    <div>
                      <p className="font-semibold text-slate-900">{leader.name}</p>
                      <p className="mt-1 text-sm text-slate-500">{leader.position}</p>
                    </div>
                    <span className="rounded-full bg-slate-100 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-[0.2em] text-slate-600">
                      View
                    </span>
                  </div>
                </Link>
              ))}
            </div>
          </aside>

          <section className="space-y-4">
            <div className="rounded-[1.5rem] border border-slate-200 bg-white p-4 shadow-sm">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm font-semibold uppercase tracking-[0.24em] text-sky-600">Location insights</p>
                  <h2 className="mt-1.5 text-xl font-semibold text-slate-900">Ratings across your area</h2>
                  <p className="mt-2 max-w-2xl text-sm leading-5 text-slate-600">
                    Showing leader ratings filtered to your location: <span className="font-semibold text-slate-900">{locationLabel}</span>.
                  </p>
                </div>
                <div className="inline-flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1.5 text-xs font-semibold text-slate-700">
                  {leaders.length} leaders
                </div>
              </div>
            </div>

            <div className="rounded-[1.5rem] border border-slate-200 bg-white p-4 shadow-sm">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h3 className="text-lg font-semibold text-slate-900">Candidate comparison</h3>
                  <p className="mt-1 text-sm text-slate-500">
                    Compare incumbents and aspirants for each position, and vote for one preferred candidate per position.
                  </p>
                </div>
                <div className="rounded-full bg-slate-100 px-3 py-1.5 text-xs font-semibold text-slate-700">
                  Updated live from votes
                </div>
              </div>

              <div className="mt-4 space-y-3">
                {positionOrder.map((position) => {
                  const poll = positionPolls[position];
                  const selectedCandidateId = userVotes[position];
                  return (
                    <div key={position} className="rounded-2xl border border-slate-200 bg-slate-50 p-3">
                      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                          <p className="font-semibold text-slate-900">{position}</p>
                          <p className="text-sm text-slate-500">Select one candidate for this position.</p>
                        </div>
                        <div className="text-sm font-medium text-slate-700">
                          {selectedCandidateId ? "Vote submitted" : "No vote yet"}
                        </div>
                      </div>
                      <div className="mt-3 space-y-2">
                        {poll?.results?.length ? poll.results.map((candidate) => {
                          const isSelected = candidate.candidateId === selectedCandidateId;
                          return (
                            <div key={candidate.candidateId} className="rounded-2xl border border-slate-200 bg-white p-3 sm:flex sm:items-center sm:justify-between">
                              <div>
                                <div className="flex flex-wrap items-center gap-2">
                                  <p className="font-semibold text-slate-900">{candidate.candidateName}</p>
                                  {candidate.isAspirant && (
                                    <span className="rounded-full bg-slate-100 px-2 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-600">
                                      Aspirant
                                    </span>
                                  )}
                                </div>
                                <p className="mt-1 text-sm text-slate-500">
                                  {candidate.party ? `${candidate.party} • ` : ""}
                                  {candidate.voteCount} vote{candidate.voteCount === 1 ? "" : "s"} • {candidate.percentage.toFixed(1)}%
                                </p>
                                <div className="mt-2 h-1.5 overflow-hidden rounded-full bg-slate-200">
                                  <div
                                    className="h-full rounded-full bg-gradient-to-r from-sky-500 to-cyan-400"
                                    style={{ width: `${candidate.percentage}%` }}
                                  />
                                </div>
                              </div>
                              <button
                                type="button"
                                disabled={Boolean(selectedCandidateId)}
                                onClick={() => handleVoteForPosition(position, candidate)}
                                className={`mt-3 inline-flex items-center justify-center rounded-full px-3 py-1.5 text-xs font-semibold transition ${isSelected ? "border border-sky-600 bg-sky-50 text-sky-700" : "bg-slate-900 text-white hover:bg-slate-800"} ${selectedCandidateId ? "opacity-60 cursor-not-allowed" : ""}`}
                              >
                                {isSelected ? "Selected" : "Vote"}
                              </button>
                            </div>
                          );
                        }) : (
                          <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-3 text-sm text-slate-500">
                            No candidates available for {position} in your location.
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </section>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
