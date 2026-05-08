package com.hms.dto.admin.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import com.hms.entity.common.ConsultationMode;

@Data
@Builder
public class DailySummaryResponse {
    private String specialisation;
    private ConsultationMode mode;
    private long totalAppointments;
    private BigDecimal totalRevenue;
}
