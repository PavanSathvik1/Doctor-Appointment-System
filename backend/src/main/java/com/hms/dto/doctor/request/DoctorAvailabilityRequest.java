package com.hms.dto.doctor.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityRequest {

    @NotNull
    private List<AvailabilityRule> rules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityRule {
        @NotNull
        private DayOfWeek dayOfWeek;
        @NotNull
        private LocalTime startTime;
        @NotNull
        private LocalTime endTime;
        
        private Integer slotDurationMinutes = 30;
    }
}
