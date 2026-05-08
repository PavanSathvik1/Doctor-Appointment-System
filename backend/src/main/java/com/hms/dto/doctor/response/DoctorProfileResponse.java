package com.hms.dto.doctor.response;

import com.hms.entity.doctor.ApprovalStatus;
import com.hms.entity.common.ConsultationMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a Doctor profile exposed to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String specialisation;
    private ConsultationMode mode;
    private String licenceNumber;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String bio;
    private String phone;
    
    // The pre-signed URL to securely access the verification document (for Admin view)
    private String documentUrl;
    
    // Status (mainly for the doctor's own dashboard or admin view)
    private String approvalStatus;
    private String rejectionReason;

    private String hospitalName;
    private String clinicAddress;
}
