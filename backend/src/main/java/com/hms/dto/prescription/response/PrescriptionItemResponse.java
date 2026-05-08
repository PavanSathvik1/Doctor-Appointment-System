package com.hms.dto.prescription.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemResponse {
    private Long id;
    private String medicineName;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private String instructions;
}
