package com.hms.elasticsearch.doctor;

import com.hms.entity.common.ConsultationMode;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Elasticsearch document representing a verified Doctor.
 * Used for fast, fuzzy searching by patients.
 */
@Document(indexName = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String firstName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String lastName;

    @Field(type = FieldType.Text)
    private String bio;

    @Field(type = FieldType.Keyword)
    private ConsultationMode mode;

    @Field(type = FieldType.Keyword)
    private String specialisation;

    @Field(type = FieldType.Integer)
    private Integer experienceYears;

    @Field(type = FieldType.Double)
    private Double consultationFee;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
