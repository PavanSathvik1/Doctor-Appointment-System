package com.hms.dto.appointment.request;

import com.hms.entity.common.ConsultationMode;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for booking a new appointment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Cannot book appointments in the past")
    private LocalDate appointmentDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    private ConsultationMode mode = ConsultationMode.OFFLINE;

    private String reasonForVisit;
}
