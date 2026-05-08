package com.hospital.management.repository;

import com.hospital.management.dto.BloodGroupCountResponseEntity;
import com.hospital.management.entity.Patient;
import com.hospital.management.entity.type.BloodGroupType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Derived Query
    Patient findByName(String name);

    // Derived Query (OR condition)
    List<Patient> findByBirthDateOrEmail(LocalDate birthDate, String email);

    // Between dates
    List<Patient> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);

    // LIKE + ORDER BY
    List<Patient> findByNameContainingIgnoreCaseOrderByIdDesc(String query);

    // JPQL positional parameter
    @Query("SELECT p FROM Patient p WHERE p.bloodGroup = ?1")
    List<Patient> findByBloodGroup(BloodGroupType bloodGroup);

    // JPQL named parameter
    @Query("SELECT p FROM Patient p WHERE p.birthDate > :birthDate")
    List<Patient> findByBornAfterDate(@Param("birthDate") LocalDate birthDate);

    // JPQL DTO projection (GROUP BY)
    @Query("""
           SELECT new com.hospital.management.dto.BloodGroupCountResponseEntity(
               p.bloodGroup, COUNT(p)
           )
           FROM Patient p
           GROUP BY p.bloodGroup
           """)
    List<BloodGroupCountResponseEntity> countEachBloodGroupType();

    // Native Query + Pagination
    @Query(value = "SELECT * FROM patient", nativeQuery = true)
    Page<Patient> findAllPatients(Pageable pageable);

    // Update query
    @Transactional
    @Modifying
    @Query("UPDATE Patient p SET p.name = :name WHERE p.id = :id")
    int updateNameWithId(@Param("name") String name,
            @Param("id") Long id);

    //Fetch join (avoid N+1)
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.appointments a LEFT JOIN FETCH a.doctor")
    List<Patient> findAllPatientWithAppointments();
}
