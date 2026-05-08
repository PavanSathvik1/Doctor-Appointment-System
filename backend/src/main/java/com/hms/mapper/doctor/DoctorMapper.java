package com.hms.mapper.doctor;

import com.hms.dto.doctor.request.DoctorRegisterRequest;
import com.hms.dto.doctor.response.DoctorProfileResponse;
import com.hms.elasticsearch.doctor.DoctorDocument;
import com.hms.entity.doctor.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Doctor entities, DTOs, and Elasticsearch documents.
 */
@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "documentS3Key", ignore = true)
    Doctor toEntity(DoctorRegisterRequest request);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "documentUrl", ignore = true) // Set manually via S3Service
    DoctorProfileResponse toResponse(Doctor doctor);

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    DoctorDocument toDocument(Doctor doctor);
}
