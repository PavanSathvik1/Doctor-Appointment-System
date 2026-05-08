package com.hms.mapper.prescription;

import com.hms.dto.prescription.request.PrescriptionItemRequest;
import com.hms.dto.prescription.request.PrescriptionRequest;
import com.hms.dto.prescription.response.PrescriptionItemResponse;
import com.hms.dto.prescription.response.PrescriptionResponse;
import com.hms.entity.appointment.Appointment;
import com.hms.entity.doctor.Doctor;
import com.hms.entity.prescription.Prescription;
import com.hms.entity.prescription.PrescriptionItem;
import com.hms.entity.user.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T13:09:44+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PrescriptionMapperImpl implements PrescriptionMapper {

    @Override
    public PrescriptionItem toItemEntity(PrescriptionItemRequest request) {
        if ( request == null ) {
            return null;
        }

        PrescriptionItem.PrescriptionItemBuilder prescriptionItem = PrescriptionItem.builder();

        prescriptionItem.dosage( request.getDosage() );
        prescriptionItem.durationDays( request.getDurationDays() );
        prescriptionItem.frequency( request.getFrequency() );
        prescriptionItem.instructions( request.getInstructions() );
        prescriptionItem.medicineName( request.getMedicineName() );

        return prescriptionItem.build();
    }

    @Override
    public Prescription toEntity(PrescriptionRequest request) {
        if ( request == null ) {
            return null;
        }

        Prescription.PrescriptionBuilder prescription = Prescription.builder();

        prescription.diagnosis( request.getDiagnosis() );
        prescription.notes( request.getNotes() );

        return prescription.build();
    }

    @Override
    public PrescriptionResponse toResponse(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }

        PrescriptionResponse prescriptionResponse = new PrescriptionResponse();

        prescriptionResponse.setAppointmentId( prescriptionAppointmentId( prescription ) );
        prescriptionResponse.setPatientId( prescriptionPatientId( prescription ) );
        prescriptionResponse.setDoctorId( prescriptionDoctorId( prescription ) );
        prescriptionResponse.setDoctorSpecialisation( prescriptionDoctorSpecialisation( prescription ) );
        prescriptionResponse.setCreatedAt( prescription.getCreatedAt() );
        prescriptionResponse.setDiagnosis( prescription.getDiagnosis() );
        prescriptionResponse.setId( prescription.getId() );
        prescriptionResponse.setItems( prescriptionItemListToPrescriptionItemResponseList( prescription.getItems() ) );
        prescriptionResponse.setNotes( prescription.getNotes() );

        prescriptionResponse.setPatientName( (prescription.getPatient().getFirstName() != null ? prescription.getPatient().getFirstName() : "") + " " + (prescription.getPatient().getLastName() != null ? prescription.getPatient().getLastName() : "") );
        prescriptionResponse.setDoctorName( "Dr. " + (prescription.getDoctor().getUser().getLastName() != null ? prescription.getDoctor().getUser().getLastName() : "Physician") );

        return prescriptionResponse;
    }

    private Long prescriptionAppointmentId(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }
        Appointment appointment = prescription.getAppointment();
        if ( appointment == null ) {
            return null;
        }
        Long id = appointment.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long prescriptionPatientId(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }
        User patient = prescription.getPatient();
        if ( patient == null ) {
            return null;
        }
        Long id = patient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long prescriptionDoctorId(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }
        Doctor doctor = prescription.getDoctor();
        if ( doctor == null ) {
            return null;
        }
        Long id = doctor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String prescriptionDoctorSpecialisation(Prescription prescription) {
        if ( prescription == null ) {
            return null;
        }
        Doctor doctor = prescription.getDoctor();
        if ( doctor == null ) {
            return null;
        }
        String specialisation = doctor.getSpecialisation();
        if ( specialisation == null ) {
            return null;
        }
        return specialisation;
    }

    protected PrescriptionItemResponse prescriptionItemToPrescriptionItemResponse(PrescriptionItem prescriptionItem) {
        if ( prescriptionItem == null ) {
            return null;
        }

        PrescriptionItemResponse prescriptionItemResponse = new PrescriptionItemResponse();

        prescriptionItemResponse.setDosage( prescriptionItem.getDosage() );
        prescriptionItemResponse.setDurationDays( prescriptionItem.getDurationDays() );
        prescriptionItemResponse.setFrequency( prescriptionItem.getFrequency() );
        prescriptionItemResponse.setId( prescriptionItem.getId() );
        prescriptionItemResponse.setInstructions( prescriptionItem.getInstructions() );
        prescriptionItemResponse.setMedicineName( prescriptionItem.getMedicineName() );

        return prescriptionItemResponse;
    }

    protected List<PrescriptionItemResponse> prescriptionItemListToPrescriptionItemResponseList(List<PrescriptionItem> list) {
        if ( list == null ) {
            return null;
        }

        List<PrescriptionItemResponse> list1 = new ArrayList<PrescriptionItemResponse>( list.size() );
        for ( PrescriptionItem prescriptionItem : list ) {
            list1.add( prescriptionItemToPrescriptionItemResponse( prescriptionItem ) );
        }

        return list1;
    }
}
