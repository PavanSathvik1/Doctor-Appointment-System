package com.hms.dto.prescription.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String notes;

    @NotEmpty(message = "At least one medicine must be prescribed")
    @Valid
    private List<PrescriptionItemRequest> items;
}
