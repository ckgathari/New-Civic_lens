package com.civiclens.dto;

import java.time.LocalDateTime;

public class PollVoteDto {
    private Long id;
    private Long voterId;
    private String voterName;
    private Long candidateId;
    private String candidateName;
    private String position;
    private Long countyId;
    private String countyName;
    private Long constituencyId;
    private String constituencyName;
    private Long wardId;
    private String wardName;
    private LocalDateTime createdAt;

    // Constructors
    public PollVoteDto() {}

    public PollVoteDto(Long id, Long voterId, String voterName, Long candidateId,
                      String candidateName, String position, Long countyId, String countyName,
                      Long constituencyId, String constituencyName, Long wardId, String wardName,
                      LocalDateTime createdAt) {
        this.id = id;
        this.voterId = voterId;
        this.voterName = voterName;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.position = position;
        this.countyId = countyId;
        this.countyName = countyName;
        this.constituencyId = constituencyId;
        this.constituencyName = constituencyName;
        this.wardId = wardId;
        this.wardName = wardName;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVoterId() {
        return voterId;
    }

    public void setVoterId(Long voterId) {
        this.voterId = voterId;
    }

    public String getVoterName() {
        return voterName;
    }

    public void setVoterName(String voterName) {
        this.voterName = voterName;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Long getCountyId() {
        return countyId;
    }

    public void setCountyId(Long countyId) {
        this.countyId = countyId;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public Long getConstituencyId() {
        return constituencyId;
    }

    public void setConstituencyId(Long constituencyId) {
        this.constituencyId = constituencyId;
    }

    public String getConstituencyName() {
        return constituencyName;
    }

    public void setConstituencyName(String constituencyName) {
        this.constituencyName = constituencyName;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}