package com.hms.service.prescription;

import com.hms.entity.doctor.Doctor;
import com.hms.entity.prescription.Prescription;
import com.hms.entity.prescription.PrescriptionItem;
import com.hms.entity.user.User;
import com.hms.exception.AppException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Service dedicated to generating PDF documents using iText 7.
 */
@Slf4j
@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Generates a professional PDF document for a given prescription.
     *
     * @param prescription the persisted prescription entity
     * @return PDF file as a byte array
     */
    public byte[] generatePrescriptionPdf(Prescription prescription) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 1. Header (Hospital Info)
            document.add(new Paragraph("HMS MEDICAL CENTER")
                    .setBold()
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));
                    
            document.add(new Paragraph("123 Healthcare Ave, Medical District | Contact: +1 800 123 4567")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // 2. Doctor and Patient Information Grid
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            
            Doctor doctor = prescription.getDoctor();
            User docUser = doctor.getUser();
            String doctorName = "Dr. " + (docUser.getFirstName() != null ? docUser.getFirstName() : "") + " " + 
                                (docUser.getLastName() != null ? docUser.getLastName() : "");
            String patientName = prescription.getPatient().getFirstName() + " " + prescription.getPatient().getLastName();
            
            infoTable.addCell(createCell("Doctor: " + doctorName + "\nSpec: " + doctor.getSpecialisation()));
            infoTable.addCell(createCell("Patient: " + patientName + "\nDate: " + prescription.getCreatedAt().format(DATE_FORMATTER)));
            
            document.add(infoTable.setMarginBottom(20));

            // 3. Clinical Details
            document.add(new Paragraph("CLINICAL DIAGNOSIS")
                    .setBold()
                    .setFontSize(12)
                    .setUnderline());
            document.add(new Paragraph(prescription.getDiagnosis()).setMarginBottom(10));

            if (prescription.getNotes() != null && !prescription.getNotes().isBlank()) {
                document.add(new Paragraph("Clinical Notes:")
                        .setItalic().setFontSize(10));
                document.add(new Paragraph(prescription.getNotes()).setMarginBottom(20));
            }

            // 4. Medication Table
            document.add(new Paragraph("PRESCRIPTION MATRICES")
                    .setBold()
                    .setFontSize(12)
                    .setUnderline()
                    .setMarginBottom(10));

            Table medicineTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 1, 3})).useAllAvailableWidth();
            
            // Table Headers
            medicineTable.addHeaderCell(createHeaderCell("Medicine"));
            medicineTable.addHeaderCell(createHeaderCell("Dosage"));
            medicineTable.addHeaderCell(createHeaderCell("Frequency"));
            medicineTable.addHeaderCell(createHeaderCell("Days"));
            medicineTable.addHeaderCell(createHeaderCell("Instructions"));

            // Table Rows
            for (PrescriptionItem item : prescription.getItems()) {
                medicineTable.addCell(new Cell().add(new Paragraph(item.getMedicineName())));
                medicineTable.addCell(new Cell().add(new Paragraph(item.getDosage())));
                medicineTable.addCell(new Cell().add(new Paragraph(item.getFrequency())));
                medicineTable.addCell(new Cell().add(new Paragraph(item.getDurationDays().toString())));
                medicineTable.addCell(new Cell().add(new Paragraph(item.getInstructions() != null ? item.getInstructions() : "")));
            }
            
            document.add(medicineTable.setMarginBottom(40));

            // 5. Footer (Signatures)
            document.add(new Paragraph("_________________________")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginRight(20));
            document.add(new Paragraph("Signature: " + doctorName)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10)
                    .setMarginRight(20));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for prescription {}: {}", prescription.getId(), e.getMessage(), e);
            throw new AppException("Failed to generate PDF document for prescription.");
        }
    }

    /**
     * Generates a simulated medical practitioner licence for doctor verification.
     *
     * @param doctor the doctor entity whose licence is being viewed
     * @return PDF file as a byte array
     */
    public byte[] generateMedicalLicensePdf(Doctor doctor) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("GOVERNMENT OF MEDICAL SERVICES")
                    .setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("MEDICAL COUNCIL OF HMS")
                    .setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30));

            document.add(new Paragraph("This is to certify that")
                    .setTextAlignment(TextAlignment.CENTER));
            
            User docUser = doctor.getUser();
            String fullName = (docUser.getFirstName() != null ? docUser.getFirstName() : "") + " " + 
                              (docUser.getLastName() != null ? docUser.getLastName() : "");
            
            document.add(new Paragraph(fullName)
                    .setBold().setFontSize(22).setUnderline()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10).setMarginBottom(10));

            document.add(new Paragraph("is duly registered as a Medical Practitioner in")
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph(doctor.getSpecialisation().toUpperCase())
                    .setBold().setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30));

            Table metaTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            metaTable.addCell(new Cell().add(new Paragraph("Licence No:")).setBorder(null).setBold());
            metaTable.addCell(new Cell().add(new Paragraph(doctor.getLicenceNumber())).setBorder(null));
            metaTable.addCell(new Cell().add(new Paragraph("Experience:")).setBorder(null).setBold());
            metaTable.addCell(new Cell().add(new Paragraph(doctor.getExperienceYears() + " Years")).setBorder(null));
            
            document.add(metaTable.setMarginBottom(50));

            document.add(new Paragraph("Issued under the authority of the National Health Services Board.")
                    .setFontSize(8).setTextAlignment(TextAlignment.CENTER).setItalic());

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating License PDF for doctor {}: {}", doctor.getId(), e.getMessage(), e);
            throw new AppException("Failed to generate License PDF.");
        }
    }

    private Cell createCell(String text) {
        return new Cell().add(new Paragraph(text)).setBorder(null);
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
    }
}
