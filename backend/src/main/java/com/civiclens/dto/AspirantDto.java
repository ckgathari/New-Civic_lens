package com.civiclens.dto;

import java.time.LocalDateTime;

public class AspirantDto {
    private String id;
    private String name;
    private String phoneNumber;
    private String email;
    private String position;
    private String bio;
    private String manifesto;
    private String profilePicture;
    private Integer countyId;
    private String countyName;
    private Integer constituencyId;
    private String constituencyName;
    private Integer wardId;
    private String wardName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public AspirantDto() {}

    public AspirantDto(String id, String name, String phoneNumber, String email, String position,
                      String bio, String manifesto, String profilePicture,
                      Integer countyId, String countyName, Integer constituencyId, String constituencyName,
                      Integer wardId, String wardName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.position = position;
        this.bio = bio;
        this.manifesto = manifesto;
        this.profilePicture = profilePicture;
        this.countyId = countyId;
        this.countyName = countyName;
        this.constituencyId = constituencyId;
        this.constituencyName = constituencyName;
        this.wardId = wardId;
        this.wardName = wardName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getManifesto() {
        return manifesto;
    }

    public void setManifesto(String manifesto) {
        this.manifesto = manifesto;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Integer getCountyId() {
        return countyId;
    }

    public void setCountyId(Integer countyId) {
        this.countyId = countyId;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public Integer getConstituencyId() {
        return constituencyId;
    }

    public void setConstituencyId(Integer constituencyId) {
        this.constituencyId = constituencyId;
    }

    public String getConstituencyName() {
        return constituencyName;
    }

    public void setConstituencyName(String constituencyName) {
        this.constituencyName = constituencyName;
    }

    public Integer getWardId() {
        return wardId;
    }

    public void setWardId(Integer wardId) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}