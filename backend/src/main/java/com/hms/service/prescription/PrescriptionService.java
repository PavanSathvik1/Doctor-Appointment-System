package com.hms.service.prescription;

import com.hms.entity.appointment.Appointment;
import com.hms.entity.appointment.AppointmentStatus;
import com.hms.repository.appointment.AppointmentRepository;
import com.hms.dto.auth.response.MessageResponse;
import com.hms.service.aws.S3Service;
import com.hms.exception.BusinessRuleException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.service.notification.NotificationService;
import com.hms.dto.prescription.request.PrescriptionRequest;
import com.hms.dto.prescription.response.PrescriptionResponse;
import com.hms.entity.prescription.Prescription;
import com.hms.mapper.prescription.PrescriptionMapper;
import com.hms.repository.prescription.PrescriptionRepository;
import com.hms.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling the business logic for Prescriptions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionMapper mapper;
    private final PdfService pdfService;
    private final S3Service s3Service;
    private final NotificationService notificationService;

    @Transactional
    public MessageResponse issuePrescription(Long doctorId, PrescriptionRequest request) {
        // 1. Validate Appointment Constraints
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", request.getAppointmentId().toString()));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new BusinessRuleException("You can only issue prescriptions for your own appointments.");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("Prescriptions can only be issued for COMPLETED appointments.");
        }

        if (prescriptionRepository.existsByAppointmentId(appointment.getId())) {
            throw new BusinessRuleException("A prescription has already been issued for this appointment.");
        }

        // 2. Map and Save Entity bounds
        Prescription prescription = mapper.toEntity(request);
        prescription.setAppointment(appointment);
        prescription.setPatient(appointment.getPatient());
        prescription.setDoctor(appointment.getDoctor());

        // Bidirectional syncing of children
        request.getItems().forEach(itemDto -> {
            prescription.addItem(mapper.toItemEntity(itemDto));
        });

        Prescription savedPrescription = prescriptionRepository.saveAndFlush(prescription);

        // 3. Generate PDF byte stream
        byte[] pdfBytes = pdfService.generatePrescriptionPdf(savedPrescription);

        // 4. Upload to S3 using byte-based method
        String filename = "prescription_" + savedPrescription.getId() + ".pdf";
        String s3Key = s3Service.uploadFile(pdfBytes, "application/pdf", filename, "prescriptions/");

        // 5. Update entity with S3 key
        savedPrescription.setPdfS3Key(s3Key);
        prescriptionRepository.save(savedPrescription);

        // 6. Async email notification
        String patientFirstName = (appointment.getPatient().getFirstName() != null) ? appointment.getPatient().getFirstName() : "Valued";
        String patientLastName = (appointment.getPatient().getLastName() != null) ? appointment.getPatient().getLastName() : "Patient";
        String patientFullName = patientFirstName + " " + patientLastName;
        
        User docUser = appointment.getDoctor().getUser();
        String docLastName = (docUser.getLastName() != null) ? docUser.getLastName() : "Doctor";
        String doctorFullName = "Dr. " + docLastName;

        notificationService.sendPrescriptionIssuedEmail(
                appointment.getPatient().getEmail(),
                patientFullName,
                doctorFullName,
                savedPrescription.getId()
        );

        return MessageResponse.builder().message("Prescription issued successfully and PDF generated.").build();
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getPatientPrescriptions(Long patientId, int page, int size) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId, PageRequest.of(page, size))
                .map(this::enrichWithPresignedUrl);
    }

    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getDoctorPrescriptions(Long doctorId, int page, int size) {
        return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId, PageRequest.of(page, size))
                .map(this::enrichWithPresignedUrl);
    }

    private PrescriptionResponse enrichWithPresignedUrl(Prescription prescription) {
        PrescriptionResponse response = mapper.toResponse(prescription);
        if (prescription.getPdfS3Key() != null) {
            try {
                response.setPdfUrl(s3Service.generatePresignedUrl(prescription.getPdfS3Key()));
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for prescription {}", prescription.getId());
            }
        }
        return response;
    }
}
