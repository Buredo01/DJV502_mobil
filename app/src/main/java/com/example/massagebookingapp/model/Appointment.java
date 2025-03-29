package com.example.massagebookingapp.model;

import com.google.firebase.Timestamp;

public class Appointment {
    private String appointmentId;
    private String userId;
    private Timestamp appointmentTimestamp;
    private Timestamp createdAt;

    // Üres konstruktor Firestore számára
    public Appointment() {}

    public Appointment(String appointmentId, String userId, Timestamp appointmentTimestamp, Timestamp createdAt) {
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.appointmentTimestamp = appointmentTimestamp;
        this.createdAt = createdAt;
    }

    // Getterek
    public String getAppointmentId() {
        return appointmentId;
    }

    public String getUserId() {
        return userId;
    }

    public Timestamp getAppointmentTimestamp() {
        return appointmentTimestamp;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setterek (ha szükségesek)
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAppointmentTimestamp(Timestamp appointmentTimestamp) {
        this.appointmentTimestamp = appointmentTimestamp;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

