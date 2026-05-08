package com.hms.controller.appointment;

import com.hms.dto.appointment.request.AppointmentRequest;
import com.hms.dto.appointment.response.AppointmentResponse;
import com.hms.dto.appointment.response.TimeSlotResponse;
import com.hms.dto.appointment.response.BookingResponse;
import com.hms.service.appointment.AppointmentService;
import com.hms.dto.auth.response.MessageResponse;
import com.hms.dto.payment.PaymentVerifyRequest;
import com.hms.entity.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for Managing and Booking Appointments.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Public endpoint to get available generation slots for a specific doctor and date.
     */
    @GetMapping("/slots")
    public ResponseEntity<List<TimeSlotResponse>> getSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
    }

    /**
     * Patient endpoint to book a new appointment.
     */
    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BookingResponse> bookAppointment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AppointmentRequest request) {
        BookingResponse response = appointmentService.bookAppointment(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Patient endpoint to verify payment after Razorpay success.
     */
    @PostMapping("/{id}/verify-payment")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<MessageResponse> verifyPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentVerifyRequest request) {
        MessageResponse response = appointmentService.verifyPayment(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a patient's own history of appointments.
     */
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Page<AppointmentResponse>> getPatientHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(user.getId(), page, size));
    }

    /**
     * Get a doctor's history/upcoming of appointments.
     */
    @GetMapping("/doctor-history")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AppointmentResponse>> getDoctorHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(user.getId(), page, size));
    }

    /**
     * Unified endpoint to change appointment status (Confirm, Cancel, Complete).
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<MessageResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        
        String newStatus = payload.get("status");
        String reason = payload.get("reason");
        
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, newStatus, reason));
    }

    /**
     * Administrative endpoint to see every appointment in the system.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(appointmentService.getAllAppointments(page, size));
    }
}
