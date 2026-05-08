package com.hms.mapper.doctor;

import com.hms.dto.doctor.request.DoctorRegisterRequest;
import com.hms.dto.doctor.response.DoctorProfileResponse;
import com.hms.elasticsearch.doctor.DoctorDocument;
import com.hms.entity.doctor.Doctor;
import com.hms.entity.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T14:17:36+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class DoctorMapperImpl implements DoctorMapper {

    @Override
    public Doctor toEntity(DoctorRegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        Doctor.DoctorBuilder doctor = Doctor.builder();

        doctor.specialisation( request.getSpecialisation() );
        doctor.licenceNumber( request.getLicenceNumber() );
        doctor.experienceYears( request.getExperienceYears() );
        doctor.consultationFee( request.getConsultationFee() );
        doctor.bio( request.getBio() );
        doctor.phone( request.getPhone() );
        doctor.mode( request.getMode() );
        doctor.hospitalName( request.getHospitalName() );
        doctor.clinicAddress( request.getClinicAddress() );

        return doctor.build();
    }

    @Override
    public DoctorProfileResponse toResponse(Doctor doctor) {
        if ( doctor == null ) {
            return null;
        }

        DoctorProfileResponse.DoctorProfileResponseBuilder doctorProfileResponse = DoctorProfileResponse.builder();

        doctorProfileResponse.firstName( doctorUserFirstName( doctor ) );
        doctorProfileResponse.lastName( doctorUserLastName( doctor ) );
        doctorProfileResponse.id( doctor.getId() );
        doctorProfileResponse.specialisation( doctor.getSpecialisation() );
        doctorProfileResponse.mode( doctor.getMode() );
        doctorProfileResponse.licenceNumber( doctor.getLicenceNumber() );
        doctorProfileResponse.experienceYears( doctor.getExperienceYears() );
        doctorProfileResponse.consultationFee( doctor.getConsultationFee() );
        doctorProfileResponse.bio( doctor.getBio() );
        doctorProfileResponse.phone( doctor.getPhone() );
        if ( doctor.getApprovalStatus() != null ) {
            doctorProfileResponse.approvalStatus( doctor.getApprovalStatus().name() );
        }
        doctorProfileResponse.rejectionReason( doctor.getRejectionReason() );
        doctorProfileResponse.hospitalName( doctor.getHospitalName() );
        doctorProfileResponse.clinicAddress( doctor.getClinicAddress() );

        return doctorProfileResponse.build();
    }

    @Override
    public DoctorDocument toDocument(Doctor doctor) {
        if ( doctor == null ) {
            return null;
        }

        DoctorDocument.DoctorDocumentBuilder doctorDocument = DoctorDocument.builder();

        doctorDocument.firstName( doctorUserFirstName( doctor ) );
        doctorDocument.lastName( doctorUserLastName( doctor ) );
        doctorDocument.id( doctor.getId() );
        doctorDocument.bio( doctor.getBio() );
        doctorDocument.mode( doctor.getMode() );
        doctorDocument.specialisation( doctor.getSpecialisation() );
        doctorDocument.experienceYears( doctor.getExperienceYears() );
        if ( doctor.getConsultationFee() != null ) {
            doctorDocument.consultationFee( doctor.getConsultationFee().doubleValue() );
        }

        return doctorDocument.build();
    }

    private String doctorUserFirstName(Doctor doctor) {
        if ( doctor == null ) {
            return null;
        }
        User user = doctor.getUser();
        if ( user == null ) {
            return null;
        }
        String firstName = user.getFirstName();
        if ( firstName == null ) {
            return null;
        }
        return firstName;
    }

    private String doctorUserLastName(Doctor doctor) {
        if ( doctor == null ) {
            return null;
        }
        User user = doctor.getUser();
        if ( user == null ) {
            return null;
        }
        String lastName = user.getLastName();
        if ( lastName == null ) {
            return null;
        }
        return lastName;
    }
}
