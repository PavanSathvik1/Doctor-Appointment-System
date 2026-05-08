package com.hms.entity.doctor;

import com.hms.entity.user.User;
import com.hms.entity.common.ConsultationMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity representing a Doctor profile in the HMS system.
 * Shares a 1:1 relationship with the generic User entity.
 */
@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(nullable = false, length = 150)
    private String specialisation;

    @Column(name = "licence_number", nullable = false, unique = true, length = 100)
    private String licenceNumber;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "document_s3_key", length = 500)
    private String documentS3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsultationMode mode = ConsultationMode.OFFLINE;

    @Column(name = "hospital_name", length = 255)
    private String hospitalName;

    @Column(name = "clinic_address", columnDefinition = "TEXT")
    private String clinicAddress;
}
