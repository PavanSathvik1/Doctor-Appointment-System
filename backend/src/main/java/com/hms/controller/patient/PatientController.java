package com.hms.controller.patient;

import com.hms.dto.patient.response.PatientOverviewResponse;
import com.hms.service.patient.PatientService;
import com.hms.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/overview")
    public ResponseEntity<PatientOverviewResponse> getOverview(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(patientService.getPatientOverview(user.getId()));
    }
}
