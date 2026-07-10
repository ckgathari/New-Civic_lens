package com.civiclens.dto;

public class AspirantCreateDto {
    private String name;
    private String phoneNumber;
    private String email;
    private String position;
    private String bio;
    private String manifesto;
    private String profilePicture;
    private Integer countyId;
    private Integer constituencyId;
    private Integer wardId;

    // Constructors
    public AspirantCreateDto() {}

    public AspirantCreateDto(String name, String phoneNumber, String email, String position,
                           String bio, String manifesto, String profilePicture,
                           Integer countyId, Integer constituencyId, Integer wardId) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.position = position;
        this.bio = bio;
        this.manifesto = manifesto;
        this.profilePicture = profilePicture;
        this.countyId = countyId;
        this.constituencyId = constituencyId;
        this.wardId = wardId;
    }

    // Getters and Setters
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

    public Integer getConstituencyId() {
        return constituencyId;
    }

    public void setConstituencyId(Integer constituencyId) {
        this.constituencyId = constituencyId;
    }

    public Integer getWardId() {
        return wardId;
    }

    public void setWardId(Integer wardId) {
        this.wardId = wardId;
    }
}