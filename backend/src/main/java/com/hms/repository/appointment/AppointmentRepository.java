package com.hms.repository.appointment;

import com.hms.entity.appointment.Appointment;
import com.hms.entity.appointment.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for {@link Appointment} operations.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Finds confirmed appointments for a specific date.
     * Used by the background scheduler for reminder emails.
     */
    List<Appointment> findByStatusAndAppointmentDate(AppointmentStatus status, LocalDate appointmentDate);

    /**
     * Used by the slot generation engine to find appointments for a doctor on a specific date
     * that occupy time slots (not cancelled).
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status NOT IN ('CANCELLED', 'NO_SHOW') ORDER BY a.startTime ASC")
    List<Appointment> findActiveAppointmentsForDoctorOnDate(
            @Param("doctorId") Long doctorId, 
            @Param("date") LocalDate date);

    /**
     * Used for double-booking prevention checking.
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status NOT IN ('CANCELLED', 'NO_SHOW') AND ((a.startTime >= :start AND a.startTime < :end) OR (a.endTime > :start AND a.endTime <= :end))")
    boolean existsOverlappingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("start") java.time.LocalTime start,
            @Param("end") java.time.LocalTime end);

    /**
     * Gets a patient's appointments.
     */
    Page<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId, Pageable pageable);

    /**
     * Gets a doctor's appointments.
     */
    Page<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId, Pageable pageable);
    
    /**
     * Gets a doctor's upcoming appointments for the dashboard.
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate >= CURRENT_DATE AND a.status IN ('PENDING', 'CONFIRMED') ORDER BY a.appointmentDate ASC, a.startTime ASC")
    Page<Appointment> findUpcomingByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    /**
     * Gets pending appointments that need action.
     */
    Page<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateAscStartTimeAsc(Long doctorId, AppointmentStatus status, Pageable pageable);

    /**
     * Counts total system appointments for a specific date.
     *
     * @param date the date to count
     * @return total count
     */
    /**
     * Finds the next upcoming appointment for a patient.
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND (a.appointmentDate > CURRENT_DATE OR (a.appointmentDate = CURRENT_DATE AND a.startTime >= CURRENT_TIME)) AND a.status IN ('PENDING', 'CONFIRMED') ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findNextAppointments(@Param("patientId") Long patientId, Pageable pageable);

    long countByPatientId(Long patientId);

    /**
     * Counts total system appointments for a specific date.
     */
    long countByAppointmentDate(java.time.LocalDate date);

    @Query("SELECT d.specialisation, a.mode, COUNT(a), SUM(d.consultationFee) " +
           "FROM Appointment a JOIN a.doctor d " +
           "WHERE a.appointmentDate = :date AND a.status = 'COMPLETED' " +
           "GROUP BY d.specialisation, a.mode")
    List<Object[]> getDailySummaries(@Param("date") LocalDate date);
}
