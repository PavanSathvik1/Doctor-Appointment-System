package com.hms.entity.appointment;

/**
 * Enumeration for the state machine of an Appointment.
 */
public enum AppointmentStatus {
    PENDING,    // Patient booked it, awaiting doctor confirmation
    CONFIRMED,  // Doctor accepted it
    COMPLETED,  // Appointment finished
    CANCELLED,  // Cancelled by patient or doctor
    NO_SHOW     // Patient did not attend
}
