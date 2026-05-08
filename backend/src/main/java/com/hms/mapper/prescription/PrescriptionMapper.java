package com.hms.mapper.prescription;

import com.hms.dto.prescription.request.PrescriptionItemRequest;
import com.hms.dto.prescription.request.PrescriptionRequest;
import com.hms.dto.prescription.response.PrescriptionResponse;
import com.hms.entity.prescription.Prescription;
import com.hms.entity.prescription.PrescriptionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prescription", ignore = true)
    PrescriptionItem toItemEntity(PrescriptionItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "pdfS3Key", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "items", ignore = true) // Handled manually for bidirectional sync
    Prescription toEntity(PrescriptionRequest request);

    @Mapping(source = "appointment.id", target = "appointmentId")
    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(target = "patientName", expression = "java((prescription.getPatient().getFirstName() != null ? prescription.getPatient().getFirstName() : \"\") + \" \" + (prescription.getPatient().getLastName() != null ? prescription.getPatient().getLastName() : \"\"))")
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(target = "doctorName", expression = "java(\"Dr. \" + (prescription.getDoctor().getUser().getLastName() != null ? prescription.getDoctor().getUser().getLastName() : \"Physician\"))")
    @Mapping(source = "doctor.specialisation", target = "doctorSpecialisation")
    @Mapping(target = "pdfUrl", ignore = true) // Injected by Service via S3
    PrescriptionResponse toResponse(Prescription prescription);
}
