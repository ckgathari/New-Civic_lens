package com.civiclens.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "poll_votes")
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    @ManyToOne
    @JoinColumn(name = "leader_candidate_id")
    private Leader leaderCandidate;

    @ManyToOne
    @JoinColumn(name = "aspirant_candidate_id")
    private Aspirant aspirantCandidate;

    @Column(name = "candidate_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CandidateType candidateType;

    @ManyToOne
    @JoinColumn(name = "county_id")
    private County county;

    @ManyToOne
    @JoinColumn(name = "constituency_id")
    private Constituency constituency;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum CandidateType {
        LEADER, ASPIRANT
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getVoter() {
        return voter;
    }

    public void setVoter(User voter) {
        this.voter = voter;
    }

    public Leader getLeaderCandidate() {
        return leaderCandidate;
    }

    public void setLeaderCandidate(Leader leaderCandidate) {
        this.leaderCandidate = leaderCandidate;
    }

    public Aspirant getAspirantCandidate() {
        return aspirantCandidate;
    }

    public void setAspirantCandidate(Aspirant aspirantCandidate) {
        this.aspirantCandidate = aspirantCandidate;
    }

    public CandidateType getCandidateType() {
        return candidateType;
    }

    public void setCandidateType(CandidateType candidateType) {
        this.candidateType = candidateType;
    }

    // Backward compatibility getter
    public Leader getCandidate() {
        return leaderCandidate;
    }

    public void setCandidate(Leader candidate) {
        this.leaderCandidate = candidate;
        this.candidateType = CandidateType.LEADER;
    }

    public County getCounty() {
        return county;
    }

    public void setCounty(County county) {
        this.county = county;
    }

    public Constituency getConstituency() {
        return constituency;
    }

    public void setConstituency(Constituency constituency) {
        this.constituency = constituency;
    }

    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}