package com.hms.dto.appointment.response;

import com.hms.entity.appointment.AppointmentStatus;
import com.hms.entity.common.ConsultationMode;
import com.hms.entity.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO encapsulating comprehensive Appointment data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    
    // Abstracted relational details
    private Long patientId;
    private String patientName;
    private String patientEmail;
    
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialisation;

    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    
    private ConsultationMode mode;
    private String meetingLink;
    private String clinicAddress;
    
    private PaymentStatus paymentStatus;
    private String razorpayOrderId;

    private AppointmentStatus status;
    private String reasonForVisit;
    private String cancellationReason;
    
    private LocalDateTime createdAt;
}
