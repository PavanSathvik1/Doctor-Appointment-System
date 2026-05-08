package com.hms.dto.appointment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO representing an available 30-minute time slot for booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isAvailable;
}
