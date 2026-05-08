package com.hms.dto.appointment.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingResponse {
    private String message;
    private String razorpayOrderId;
    private Long appointmentId;
}
