package com.hms.mapper.appointment;

import com.hms.dto.appointment.response.AppointmentResponse;
import com.hms.entity.appointment.Appointment;
import com.hms.entity.doctor.Doctor;
import com.hms.entity.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T13:09:43+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AppointmentMapperImpl implements AppointmentMapper {

    @Override
    public AppointmentResponse toResponse(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }

        AppointmentResponse.AppointmentResponseBuilder appointmentResponse = AppointmentResponse.builder();

        appointmentResponse.patientId( appointmentPatientId( appointment ) );
        appointmentResponse.patientEmail( appointmentPatientEmail( appointment ) );
        appointmentResponse.doctorId( appointmentDoctorId( appointment ) );
        appointmentResponse.doctorSpecialisation( appointmentDoctorSpecialisation( appointment ) );
        appointmentResponse.appointmentDate( appointment.getAppointmentDate() );
        appointmentResponse.cancellationReason( appointment.getCancellationReason() );
        appointmentResponse.clinicAddress( appointment.getClinicAddress() );
        appointmentResponse.createdAt( appointment.getCreatedAt() );
        appointmentResponse.endTime( appointment.getEndTime() );
        appointmentResponse.id( appointment.getId() );
        appointmentResponse.meetingLink( appointment.getMeetingLink() );
        appointmentResponse.mode( appointment.getMode() );
        appointmentResponse.paymentStatus( appointment.getPaymentStatus() );
        appointmentResponse.razorpayOrderId( appointment.getRazorpayOrderId() );
        appointmentResponse.reasonForVisit( appointment.getReasonForVisit() );
        appointmentResponse.startTime( appointment.getStartTime() );
        appointmentResponse.status( appointment.getStatus() );

        appointmentResponse.patientName( appointment.getPatient().getFirstName() != null && !appointment.getPatient().getFirstName().isBlank() ? appointment.getPatient().getFirstName() + " " + (appointment.getPatient().getLastName() != null ? appointment.getPatient().getLastName() : "") : "Patient (" + appointment.getPatient().getEmail() + ")" );
        appointmentResponse.doctorName( "Dr. " + (appointment.getDoctor().getUser().getLastName() != null ? appointment.getDoctor().getUser().getLastName() : "Physician") );

        return appointmentResponse.build();
    }

    private Long appointmentPatientId(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        User patient = appointment.getPatient();
        if ( patient == null ) {
            return null;
        }
        Long id = patient.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String appointmentPatientEmail(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        User patient = appointment.getPatient();
        if ( patient == null ) {
            return null;
        }
        String email = patient.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }

    private Long appointmentDoctorId(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Doctor doctor = appointment.getDoctor();
        if ( doctor == null ) {
            return null;
        }
        Long id = doctor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String appointmentDoctorSpecialisation(Appointment appointment) {
        if ( appointment == null ) {
            return null;
        }
        Doctor doctor = appointment.getDoctor();
        if ( doctor == null ) {
            return null;
        }
        String specialisation = doctor.getSpecialisation();
        if ( specialisation == null ) {
            return null;
        }
        return specialisation;
    }
}
