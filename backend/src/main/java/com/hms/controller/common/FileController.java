package com.hms.controller.common;

import com.hms.repository.doctor.DoctorRepository;
import com.hms.repository.prescription.PrescriptionRepository;
import com.hms.service.prescription.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller to serve files from S3 or mock files in local development.
 * Resolves the issue where 'http://localhost/' links fail due to port/CORS mismatches.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;
    private final PdfService pdfService;
    private static final String LOCAL_STORAGE_DIR = "local-storage/";

    /**
     * Serves a file by its S3 key. 
     * In local development (keys containing 'dummy-'), it returns a real PDF if it's a prescription
     * or doctor licence, or a mock placeholder for other files.
     */
    @GetMapping("/view/{*key}")
    public ResponseEntity<byte[]> viewFile(@PathVariable String key) {
        log.info("Request to view file with key: {}", key);

        if (key.contains("dummy-")) {
            return serveMockFile(key);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private ResponseEntity<byte[]> serveMockFile(String key) {
        String fullKey = key.startsWith("/") ? key.substring(1) : key;
        
        // 1. Try to serve the actual file from disk if it exists
        try {
            Path filePath = Paths.get(LOCAL_STORAGE_DIR + fullKey);
            if (Files.exists(filePath)) {
                log.info("Found physical file on disk at: {}. Serving actual content.", filePath);
                byte[] data = Files.readAllBytes(filePath);
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = fullKey.toLowerCase().endsWith(".pdf") ? "application/pdf" : "application/octet-stream";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                        .body(data);
            }
        } catch (Exception e) {
            log.error("Error reading local dummy file", e);
        }

        // 2. Try to extract prescription ID if it's a prescription key
        if (fullKey.toLowerCase().contains("prescription")) {
            try {
                Pattern pattern = Pattern.compile("prescription_(\\d+)\\.pdf");
                Matcher matcher = pattern.matcher(fullKey);
                
                if (matcher.find()) {
                    Long prescriptionId = Long.parseLong(matcher.group(1));
                    log.info("Extracted prescription ID {} from dummy key", prescriptionId);
                    
                    return prescriptionRepository.findByIdFull(prescriptionId)
                            .map(pdfService::generatePrescriptionPdf)
                            .map(data -> ResponseEntity.ok()
                                    .contentType(MediaType.APPLICATION_PDF)
                                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"prescription.pdf\"")
                                    .body(data))
                            .orElseGet(() -> createFallbackPdf(fullKey));
                }
            } catch (Exception e) {
                log.error("Failed to generate real prescription PDF for dummy key, falling back to mock", e);
            }
        }

        // 2. Try to handle doctor licence verification documents
        if (fullKey.toLowerCase().contains("doctor-applications")) {
            try {
                log.info("Attempting reverse lookup for doctor licence with key: {}", fullKey);
                return doctorRepository.findByDocumentS3Key(fullKey)
                        .map(pdfService::generateMedicalLicensePdf)
                        .map(data -> ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_PDF)
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"doctor_licence.pdf\"")
                                .body(data))
                        .orElseGet(() -> createFallbackPdf(fullKey));
            } catch (Exception e) {
                log.error("Failed to generate real doctor licence PDF for dummy key, falling back to mock", e);
            }
        }

        return createFallbackPdf(fullKey);
    }

    private ResponseEntity<byte[]> createFallbackPdf(String key) {
        boolean isPdf = key.toLowerCase().contains(".pdf");
        
        if (isPdf) {
            String mockPdfContent = "%PDF-1.4\n1 0 obj\n<< /Title (Mock Document) /Creator (HMS) >>\nendobj\n2 0 obj\n<< /Type /Catalog /Pages 3 0 R >>\nendobj\n3 0 obj\n<< /Type /Pages /Kids [4 0 R] /Count 1 >>\nendobj\n4 0 obj\n<< /Type /Page /Parent 3 0 R /MediaBox [0 0 612 792] /Contents 5 0 R >>\nendobj\n5 0 obj\n<< /Length 44 >>\nstream\nBT /F1 24 Tf 100 700 Td (HMS Mock Document: " + key + ") Tj ET\nendstream\nendobj\nxref\n0 6\n0000000000 65535 f\n0000000010 00000 n\n0000000062 00000 n\n0000000111 00000 n\n0000000173 00000 n\n0000000271 00000 n\ntrailer\n<< /Size 6 /Root 2 0 R >>\nstartxref\n365\n%%EOF";
            byte[] data = mockPdfContent.getBytes(StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"mock_document.pdf\"")
                    .body(data);
        } else {
            String mockImgContent = "<svg width='400' height='200' xmlns='http://www.w3.org/2000/svg'><rect width='100%' height='100%' fill='#f3f4f6'/><text x='50%' y='50%' font-family='Arial' font-size='20' fill='#6b7280' text-anchor='middle' dominant-baseline='middle'>Mock Document: " + key + "</text></svg>";
            byte[] data = mockImgContent.getBytes(StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/svg+xml"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"mock_file.svg\"")
                    .body(data);
        }
    }
}
