package com.example.maziyyah.mood_tracker.model;

public class LovedOneDTO {

    private String lovedOneId;
    private String name;
    private String contact;
    private String relationship;
    private String status;

    public String getLovedOneId() {
        return lovedOneId;
    }

    public void setLovedOneId(String lovedOneId) {
        this.lovedOneId = lovedOneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String telegramStatus) {
        this.status = telegramStatus;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

}
