package com.hms.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private long totalPatients;
    private long totalDoctors;
    private long todayAppointments;
    private long pendingDoctorRegistrations;
    private List<DailySummaryResponse> dailySummaries;
}
