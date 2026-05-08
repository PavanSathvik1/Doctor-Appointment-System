package com.hms.repository.doctor;

import com.hms.entity.doctor.ApprovalStatus;
import com.hms.entity.doctor.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for {@link Doctor} entity operations.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * Finds a doctor by their document storage key.
     * Used for identifying authors of verification documents in mock dev mode.
     */
    Optional<Doctor> findByDocumentS3Key(String documentS3Key);

    /**
     * Checks if a doctor exists with the given licence number.
     *
     * @param licenceNumber the licence number to check
     * @return true if it exists
     */
    boolean existsByLicenceNumber(String licenceNumber);

    /**
     * Finds doctors paged by their approval status.
     *
     * @param approvalStatus the status to filter by (e.g. PENDING for admin dashboard)
     * @param pageable       pagination info
     * @return a page of doctors
     */
    Page<Doctor> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);

    /**
     * Counts doctors by their approval status.
     *
     * @param approvalStatus the status to count
     * @return the number of doctors with the given status
     */
    long countByApprovalStatus(ApprovalStatus approvalStatus);
}
