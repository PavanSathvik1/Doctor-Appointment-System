package com.hms.controller.prescription;

import com.hms.dto.auth.response.MessageResponse;
import com.hms.dto.prescription.request.PrescriptionRequest;
import com.hms.dto.prescription.response.PrescriptionResponse;
import com.hms.service.prescription.PrescriptionService;
import com.hms.entity.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Prescription endpoints.
 */
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * Doctor endpoint to formulate and issue a prescription for a completed appointment.
     */
    @PostMapping("/issue")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MessageResponse> issuePrescription(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PrescriptionRequest request) {
            
        MessageResponse response = prescriptionService.issuePrescription(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Patient endpoint to get their prescription history.
     */
    @GetMapping("/my-prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Page<PrescriptionResponse>> getPatientPrescriptions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions(user.getId(), page, size));
    }

    /**
     * Doctor endpoint to view the prescriptions they have issued over time.
     */
    @GetMapping("/issued-history")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<PrescriptionResponse>> getDoctorPrescriptions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
            
        return ResponseEntity.ok(prescriptionService.getDoctorPrescriptions(user.getId(), page, size));
    }
}
