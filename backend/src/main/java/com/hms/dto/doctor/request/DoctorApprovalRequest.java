package com.hms.dto.doctor.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used by Admin to approve or reject a doctor application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorApprovalRequest {

    @NotNull(message = "Approval decision (true for approve, false for reject) is required")
    private Boolean approve;

    // Required if approve == false
    private String rejectionReason;
}
