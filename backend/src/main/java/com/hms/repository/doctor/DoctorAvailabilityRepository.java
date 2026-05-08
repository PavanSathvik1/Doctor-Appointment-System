package com.hms.repository.doctor;

import com.hms.entity.doctor.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link DoctorAvailability} entity operations.
 */
@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    /**
     * Finds all active availability schedules for a specific doctor.
     *
     * @param doctorId the doctor's ID
     * @return list of availability rules
     */
    List<DoctorAvailability> findByDoctorIdAndIsActiveTrue(Long doctorId);

    /**
     * Finds an active availability rule for a doctor on a specific day of the week.
     * Used by the slot generation service to compute available slots.
     *
     * @param doctorId  the doctor's ID
     * @param dayOfWeek the day of the week
     * @return the availability rule if present
     */
    Optional<DoctorAvailability> findByDoctorIdAndDayOfWeekAndIsActiveTrue(Long doctorId, DayOfWeek dayOfWeek);
    
    /**
     * Delete all availability rules for a doctor. Used when overwriting schedules.
     *
     * @param doctorId the doctor's ID
     */
    void deleteByDoctorId(Long doctorId);
}
