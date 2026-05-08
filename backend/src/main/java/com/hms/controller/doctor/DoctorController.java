package com.hms.controller.doctor;

import com.hms.dto.auth.response.MessageResponse;
import com.hms.dto.doctor.request.DoctorApprovalRequest;
import com.hms.dto.doctor.request.DoctorRegisterRequest;
import com.hms.dto.doctor.request.DoctorAvailabilityRequest;
import com.hms.dto.doctor.request.DoctorProfileUpdateRequest;
import com.hms.dto.doctor.response.DoctorProfileResponse;
import com.hms.service.doctor.DoctorService;
import com.hms.entity.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.hms.elasticsearch.doctor.DoctorElasticsearchRepository;
import com.hms.elasticsearch.doctor.DoctorDocument;
import com.hms.entity.common.ConsultationMode;

/**
 * Controller for Doctor registration, profiles, and Admin approvals.
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorElasticsearchRepository elasticsearchRepository;

    /**
     * Public endpoint for doctors to apply to join the hospital.
     * Accepts multipart/form-data.
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @ModelAttribute DoctorRegisterRequest request) {
        MessageResponse response = doctorService.registerDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Public or Patient endpoint to get a specific doctor's profile.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileResponse> getDoctorProfile(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorProfile(id));
    }

    /**
     * Public endpoint to search doctors quickly using Elasticsearch.
     */
    @GetMapping("/search")
    public ResponseEntity<List<DoctorDocument>> searchDoctors(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) ConsultationMode mode) {
        
        List<DoctorDocument> results;
        
        if ((query == null || query.isBlank()) && mode == null) {
            Iterable<DoctorDocument> allDocs = elasticsearchRepository.findAll();
            results = (allDocs instanceof Page)
                    ? ((Page<DoctorDocument>) allDocs).getContent()
                    : StreamSupport.stream(allDocs.spliterator(), false).collect(Collectors.toList());
        } else if (query == null || query.isBlank()) {
            results = elasticsearchRepository.findByMode(mode);
        } else if (mode == null) {
            results = elasticsearchRepository
                    .findByFirstNameContainingOrLastNameContainingOrSpecialisationContaining(query, query, query);
        } else {
            results = elasticsearchRepository
                    .findByModeAndFirstNameContainingOrModeAndLastNameContainingOrModeAndSpecialisationContaining(
                            mode, query, mode, query, mode, query);
        }
                
        return ResponseEntity.ok(results);
    }

    /**
     * Admin endpoint to view pending doctor applications.
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DoctorProfileResponse>> getPendingApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<DoctorProfileResponse> response = doctorService.getPendingApplications(page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin endpoint to approve or reject a doctor application.
     */
    @PutMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> processApplication(
            @PathVariable Long id, 
            @Valid @RequestBody DoctorApprovalRequest request) {
            
        MessageResponse response = doctorService.processApplication(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Doctor endpoint to update their own working hours schedule.
     */
    @PostMapping("/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MessageResponse> updateAvailability(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
            
        MessageResponse response = doctorService.updateAvailability(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Doctor endpoint to update their own professional profile.
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MessageResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DoctorProfileUpdateRequest request) {
            
        MessageResponse response = doctorService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(response);
    }
}
