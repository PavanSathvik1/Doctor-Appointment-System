package com.hms.elasticsearch.doctor;

import com.hms.entity.common.ConsultationMode;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for fast doctor search operations.
 */
@Repository
public interface DoctorElasticsearchRepository extends ElasticsearchRepository<DoctorDocument, Long> {

    /**
     * Fuzzy search by full name (first name OR last name) OR specialisation.
     */
    List<DoctorDocument> findByFirstNameContainingOrLastNameContainingOrSpecialisationContaining(
            String firstName, String lastName, String specialisation);
            
    /**
     * Exact search by specialisation.
     */
    List<DoctorDocument> findBySpecialisation(String specialisation);

    /**
     * Filter by mode.
     */
    List<DoctorDocument> findByMode(ConsultationMode mode);

    /**
     * Filter by query AND mode.
     */
    List<DoctorDocument> findByModeAndFirstNameContainingOrModeAndLastNameContainingOrModeAndSpecialisationContaining(
            ConsultationMode mode1, String firstName,
            ConsultationMode mode2, String lastName,
            ConsultationMode mode3, String specialisation);
}
