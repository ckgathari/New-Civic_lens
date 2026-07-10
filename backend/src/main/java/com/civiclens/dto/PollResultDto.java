package com.civiclens.dto;

public class PollResultDto {
    private String candidateId;
    private String candidateName;
    private String party;
    private boolean isAspirant;
    private int voteCount;
    private double percentage;

    // Constructors
    public PollResultDto() {}

    public PollResultDto(String candidateId, String candidateName, String party,
                        boolean isAspirant, int voteCount, double percentage) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.party = party;
        this.isAspirant = isAspirant;
        this.voteCount = voteCount;
        this.percentage = percentage;
    }

    // Getters and Setters
    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public boolean isAspirant() {
        return isAspirant;
    }

    public void setAspirant(boolean aspirant) {
        isAspirant = aspirant;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}