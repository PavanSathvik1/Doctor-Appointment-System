package com.hms.service.patient;

import com.hms.mapper.appointment.AppointmentMapper;
import com.hms.repository.appointment.AppointmentRepository;
import com.hms.dto.patient.response.PatientOverviewResponse;
import com.hms.mapper.prescription.PrescriptionMapper;
import com.hms.repository.prescription.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentMapper appointmentMapper;
    private final PrescriptionMapper prescriptionMapper;

    @Override
    @Transactional(readOnly = true)
    public PatientOverviewResponse getPatientOverview(Long patientId) {
        
        var nextAppts = appointmentRepository.findNextAppointments(patientId, PageRequest.of(0, 1));
        var latestPrescriptions = prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId, PageRequest.of(0, 1));
        
        return PatientOverviewResponse.builder()
                .nextAppointment(nextAppts.isEmpty() ? null : appointmentMapper.toResponse(nextAppts.get(0)))
                .latestPrescription(latestPrescriptions.isEmpty() ? null : prescriptionMapper.toResponse(latestPrescriptions.getContent().get(0)))
                .totalAppointments(appointmentRepository.countByPatientId(patientId))
                .totalPrescriptions(prescriptionRepository.countByPatientId(patientId))
                .build();
    }
}
