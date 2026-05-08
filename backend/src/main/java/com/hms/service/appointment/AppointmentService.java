package com.hms.service.appointment;

import com.hms.dto.appointment.request.AppointmentRequest;
import com.hms.dto.appointment.response.AppointmentResponse;
import com.hms.dto.appointment.response.BookingResponse;
import com.hms.dto.appointment.response.TimeSlotResponse;
import com.hms.dto.auth.response.MessageResponse;
import com.hms.dto.payment.PaymentVerifyRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Appointment management and Slot generation.
 */
public interface AppointmentService {

    /**
     * Dynamically generates available time slots for a doctor on a specific date.
     * Considers the doctor's general availability rules minus existing booked appointments.
     *
     * @param doctorId the doctor's ID
     * @param date     the requested date
     * @return a list of slots with their availability status
     */
    List<TimeSlotResponse> getAvailableSlots(Long doctorId, LocalDate date);

    /**
     * Books a new appointment for a patient.
     * Enforces double-booking constraints and slot boundaries.
     *
     * @param patientId the patient booking the appointment
     * @param request   the appointment details
     * @return a success message
     */
    BookingResponse bookAppointment(Long patientId, AppointmentRequest request);

    /**
     * Changes the status of an appointment.
     * E.g. Doctor confirms or modifies it, Patient cancels it.
     * Sends appropriate notification emails based on transition.
     *
     * @param appointmentId the appointment ID
     * @param newStatus     the new status enum string
     * @param reason        optional reason, required if CANCELLED
     * @return a success message
     */
    MessageResponse updateAppointmentStatus(Long appointmentId, String newStatus, String reason);

    MessageResponse verifyPayment(Long appointmentId, PaymentVerifyRequest request);

    /**
     * Gets a paginated list of appointments for a specific patient.
     *
     * @param patientId patient ID
     * @param page      page index
     * @param size      page size
     * @return page of appointments
     */
    Page<AppointmentResponse> getPatientAppointments(Long patientId, int page, int size);

    /**
     * Gets a paginated list of appointments for a specific doctor.
     *
     * @param doctorId doctor ID
     * @param page     page index
     * @param size     page size
     * @return page of appointments
     */
    Page<AppointmentResponse> getDoctorAppointments(Long doctorId, int page, int size);
    
    /**
     * Gets upcoming active appointments for the doctor dashboard.
     */
    Page<AppointmentResponse> getDoctorDashboardUpcoming(Long doctorId, int page, int size);

    /**
     * Admin method to get all system appointments.
     */
    Page<AppointmentResponse> getAllAppointments(int page, int size);
}
