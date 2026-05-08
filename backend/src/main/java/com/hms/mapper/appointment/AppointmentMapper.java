package com.hms.mapper.appointment;

import com.hms.dto.appointment.response.AppointmentResponse;
import com.hms.entity.appointment.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Appointment entities.
 */
@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", expression = "java(appointment.getPatient().getFirstName() != null && !appointment.getPatient().getFirstName().isBlank() ? appointment.getPatient().getFirstName() + \" \" + (appointment.getPatient().getLastName() != null ? appointment.getPatient().getLastName() : \"\") : \"Patient (\" + appointment.getPatient().getEmail() + \")\")")
    @Mapping(target = "patientEmail", source = "patient.email")
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "doctorName", expression = "java(\"Dr. \" + (appointment.getDoctor().getUser().getLastName() != null ? appointment.getDoctor().getUser().getLastName() : \"Physician\"))")
    @Mapping(target = "doctorSpecialisation", source = "doctor.specialisation")
    AppointmentResponse toResponse(Appointment appointment);
}
