package com.hms.dto.doctor.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import com.hms.entity.common.ConsultationMode;

import java.math.BigDecimal;

/**
 * Request DTO for Doctor Registration.
 * Usually sent as multipart/form-data where the document is the file
 * and other fields are text parts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRegisterRequest {

    // Inherited user fields
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;

    // Doctor profile fields
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Specialisation is required")
    private String specialisation;

    @NotBlank(message = "Licence number is required")
    private String licenceNumber;

    @NotNull(message = "Experience years is required")
    @Positive(message = "Experience must be greater than 0")
    private Integer experienceYears;

    @NotNull(message = "Consultation fee is required")
    @Positive(message = "Fee must be positive")
    private BigDecimal consultationFee;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    private ConsultationMode mode = ConsultationMode.OFFLINE;

    private String hospitalName;

    private String clinicAddress;

    private String bio;

    // The validation document (e.g. PDF of medical licence)
    @NotNull(message = "Verification document (PDF/Image) is required")
    private MultipartFile document;
}
