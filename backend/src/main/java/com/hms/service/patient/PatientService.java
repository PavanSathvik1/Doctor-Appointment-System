package com.hms.service.patient;

import com.hms.dto.patient.response.PatientOverviewResponse;

public interface PatientService {
    PatientOverviewResponse getPatientOverview(Long patientId);
}
