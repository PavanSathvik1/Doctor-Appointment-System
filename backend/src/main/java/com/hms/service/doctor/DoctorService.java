package com.hms.service.doctor;

import com.hms.dto.auth.response.MessageResponse;
import com.hms.dto.doctor.request.DoctorApprovalRequest;
import com.hms.dto.doctor.request.DoctorRegisterRequest;
import com.hms.dto.doctor.request.DoctorAvailabilityRequest;
import com.hms.dto.doctor.request.DoctorProfileUpdateRequest;
import com.hms.dto.doctor.response.DoctorProfileResponse;
import org.springframework.data.domain.Page;

/**
 * Service interface for Doctor module operations.
 */
public interface DoctorService {

    /**
     * Registers a new doctor. Creates the User (role DOCTOR, status PENDING),
     * uploads the validation document to S3, and creates the Doctor entity.
     *
     * @param request the multipart registration request
     * @return success message
     */
    MessageResponse registerDoctor(DoctorRegisterRequest request);

    /**
     * Gets a page of pending doctor applications for the Admin dashboard.
     * Generates temporary S3 presigned URLs for the documents.
     *
     * @param page page number
     * @param size page size
     * @return page of doctor profiles
     */
    Page<DoctorProfileResponse> getPendingApplications(int page, int size);

    /**
     * Processes an admin's decision on a doctor application.
     * If approved: updates status, activates user, indexes to ES, sends approval email.
     * If rejected: updates status, sets reason, sends rejection email.
     *
     * @param doctorId the doctor ID
     * @param request  the approval decision
     * @return success message
     */
    MessageResponse processApplication(Long doctorId, DoctorApprovalRequest request);

    /**
     * Gets the profile of a doctor by ID.
     *
     * @param doctorId the ID
     * @return the profile response
     */
    DoctorProfileResponse getDoctorProfile(Long doctorId);

    /**
     * Updates the doctor's weekly availability schedule.
     */
    MessageResponse updateAvailability(Long doctorId, DoctorAvailabilityRequest request);

    /**
     * Allows a doctor to update their own professional profile.
     *
     * @param userId the user ID
     * @param request the update data
     * @return success message
     */
    MessageResponse updateProfile(Long userId, DoctorProfileUpdateRequest request);
}
