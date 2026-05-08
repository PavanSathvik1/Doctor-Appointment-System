package com.hms.dto.doctor.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorProfileUpdateRequest {
    
    @NotBlank(message = "Specialisation is required")
    private String specialisation;

    @NotBlank(message = "Bio is required")
    private String bio;

    @NotNull(message = "Consultation fee is required")
    private Double consultationFee;

    private String phoneNumber;
}
