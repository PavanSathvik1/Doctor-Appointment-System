package com.hms.repository.prescription;

import com.hms.entity.prescription.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("SELECT p FROM Prescription p LEFT JOIN FETCH p.patient LEFT JOIN FETCH p.doctor LEFT JOIN FETCH p.items WHERE p.id = :id")
    Optional<Prescription> findByIdFull(@Param("id") Long id);

    // A patient can retrieve all their prescriptions
    Page<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    // A doctor can retrieve prescriptions they've issued
    Page<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId, Pageable pageable);
    
    // Quick lookup to see if an appointment already has a prescription
    boolean existsByAppointmentId(Long appointmentId);
    
    // Fetch a prescription by its attached appointment
    Optional<Prescription> findByAppointmentId(Long appointmentId);

    long countByPatientId(Long patientId);
}
