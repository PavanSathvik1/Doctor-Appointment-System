package com.hms.dto.prescription.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long appointmentId;
    
    private Long patientId;
    private String patientName;
    
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialisation;
    
    private String diagnosis;
    private String notes;
    private String pdfUrl; // Transient pre-signed download URL
    
    private List<PrescriptionItemResponse> items;
    private LocalDateTime createdAt;
}
