package com.hms.dto.patient.response;

import com.hms.dto.appointment.response.AppointmentResponse;
import com.hms.dto.prescription.response.PrescriptionResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientOverviewResponse {
    private AppointmentResponse nextAppointment;
    private PrescriptionResponse latestPrescription;
    private long totalAppointments;
    private long totalPrescriptions;
}
