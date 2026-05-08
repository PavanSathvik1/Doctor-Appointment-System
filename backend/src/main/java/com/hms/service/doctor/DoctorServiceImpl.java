package com.hms.service.doctor;

import com.hms.dto.auth.response.MessageResponse;
import com.hms.service.aws.S3Service;
import com.hms.exception.BusinessRuleException;
import com.hms.exception.ConflictException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.dto.doctor.request.DoctorApprovalRequest;
import com.hms.dto.doctor.request.DoctorRegisterRequest;
import com.hms.dto.doctor.request.DoctorAvailabilityRequest;
import com.hms.dto.doctor.request.DoctorProfileUpdateRequest;
import com.hms.dto.doctor.response.DoctorProfileResponse;
import com.hms.elasticsearch.doctor.DoctorDocument;
import com.hms.elasticsearch.doctor.DoctorElasticsearchRepository;
import com.hms.entity.doctor.ApprovalStatus;
import com.hms.entity.doctor.Doctor;
import com.hms.entity.doctor.DoctorAvailability;
import com.hms.mapper.doctor.DoctorMapper;
import com.hms.repository.doctor.DoctorAvailabilityRepository;
import com.hms.repository.doctor.DoctorRepository;
import com.hms.service.notification.NotificationService;
import com.hms.entity.user.Role;
import com.hms.entity.user.User;
import com.hms.entity.user.UserStatus;
import com.hms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the DoctorService.
 * Handles the complex workflow of doctor registration, S3 uploads, 
 * admin approval, and Elasticsearch indexing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private final DoctorElasticsearchRepository elasticsearchRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Override
    @Transactional
    public MessageResponse registerDoctor(DoctorRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }
        if (doctorRepository.existsByLicenceNumber(request.getLicenceNumber())) {
            throw new ConflictException("Licence number is already registered");
        }

        // 1. Create and save the generic User (PENDING status)
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .status(UserStatus.PENDING)
                .build();
        user = userRepository.save(user);

        // 2. Upload verification document to S3
        String s3Key = null;
        try {
            s3Key = s3Service.uploadFile(request.getDocument(), "doctor-applications/");
        } catch (Exception e) {
            log.error("Failed to upload document for doctor: {}", request.getEmail(), e);
            throw new BusinessRuleException("Failed to upload verification document: " + e.getMessage());
        }

        // 3. Create and save the Doctor entity
        Doctor doctor = doctorMapper.toEntity(request);
        doctor.setUser(user);
        // Note: With @MapsId, setId(user.getId()) is handled automatically by Hibernate.
        // Manually setting it can cause detached-to-transient session errors.
        doctor.setApprovalStatus(ApprovalStatus.PENDING);
        doctor.setDocumentS3Key(s3Key);
        
        doctorRepository.save(doctor);
        log.info("Registered new doctor pending approval: {}", doctor.getUser().getEmail());

        return MessageResponse.builder()
                .message("Application submitted successfully. Our team will review your application within 48 hours.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorProfileResponse> getPendingApplications(int page, int size) {
        Page<Doctor> pendingDoctors = doctorRepository.findByApprovalStatus(
                ApprovalStatus.PENDING, PageRequest.of(page, size));

        return pendingDoctors.map(doctor -> {
            DoctorProfileResponse response = doctorMapper.toResponse(doctor);
            // Generate temporary presigned URL for admins to securely view the uploaded licence
            if (doctor.getDocumentS3Key() != null) {
                try {
                    response.setDocumentUrl(s3Service.generatePresignedUrl(doctor.getDocumentS3Key()));
                } catch (Exception e) {
                    log.warn("Could not generate presigned URL for doctor {}", doctor.getId());
                }
            }
            return response;
        });
    }

    @Override
    @Transactional
    public MessageResponse processApplication(Long doctorId, DoctorApprovalRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId.toString()));

        if (doctor.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessRuleException("Application has already been processed.");
        }

        User user = doctor.getUser();

        if (Boolean.TRUE.equals(request.getApprove())) {
            // Approve workflow
            doctor.setApprovalStatus(ApprovalStatus.APPROVED);
            user.setStatus(UserStatus.ACTIVE);
            
        // Index to Elasticsearch for fast search
            DoctorDocument doc = doctorMapper.toDocument(doctor);
            try {
                elasticsearchRepository.save(doc);
                log.info("Indexed doctor {} to Elasticsearch", doctor.getId());
            } catch (Exception e) {
                log.error("Failed to index doctor {} in Elasticsearch. Check cluster connection.", doctor.getId(), e);
            }

            // Send notification
            User docUser = doctor.getUser();
            String docLastName = (docUser.getLastName() != null) ? docUser.getLastName() : "Doctor";
            String docFullName = "Dr. " + docLastName;
            
            notificationService.sendDoctorApprovedEmail(user.getEmail(), docFullName);
            log.info("Doctor application approved for {}", user.getEmail());
            
            return MessageResponse.builder().message("Doctor application approved successfully.").build();
            
        } else {
            // Reject workflow
            doctor.setApprovalStatus(ApprovalStatus.REJECTED);
            doctor.setRejectionReason(request.getRejectionReason());
            user.setStatus(UserStatus.SUSPENDED); // Account cannot login
            
            // Send notification
            User docUser = doctor.getUser();
            String docLastName = (docUser.getLastName() != null) ? docUser.getLastName() : "Doctor";
            String docFullName = "Dr. " + docLastName;
            
            notificationService.sendDoctorRejectedEmail(user.getEmail(), docFullName, request.getRejectionReason());
            log.info("Doctor application rejected for {}", user.getEmail());
            
            return MessageResponse.builder().message("Doctor application rejected.").build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorProfile(Long doctorId) {
        // Also used for doctor public profiles
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId.toString()));
                
        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional
    public MessageResponse updateAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", doctorId.toString()));

        // Clear existing availability to overwrite
        availabilityRepository.deleteByDoctorId(doctorId);

        // Map and save new rules
        java.util.List<DoctorAvailability> newRules = request.getRules().stream().map(rule -> {
            DoctorAvailability availability = new DoctorAvailability();
            availability.setDoctor(doctor);
            availability.setDayOfWeek(rule.getDayOfWeek());
            availability.setStartTime(rule.getStartTime());
            availability.setEndTime(rule.getEndTime());
            availability.setSlotDurationMinutes(rule.getSlotDurationMinutes() != null ? rule.getSlotDurationMinutes() : 30);
            availability.setIsActive(true);
            return availability;
        }).toList();

        availabilityRepository.saveAll(newRules);
        
        return MessageResponse.builder().message("Availability schedule updated successfully.").build();
    }

    @Override
    @Transactional
    public MessageResponse updateProfile(Long userId, DoctorProfileUpdateRequest request) {
        Doctor doctor = doctorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", userId.toString()));

        doctor.setSpecialisation(request.getSpecialisation());
        doctor.setBio(request.getBio());
        doctor.setConsultationFee(java.math.BigDecimal.valueOf(request.getConsultationFee()));
        doctor.setPhone(request.getPhoneNumber());

        doctorRepository.save(doctor);

        // Sync to Elasticsearch
        try {
            DoctorDocument doc = doctorMapper.toDocument(doctor);
            elasticsearchRepository.save(doc);
        } catch (Exception e) {
            log.error("Failed to sync profile update to Elasticsearch for doctor {}", userId, e);
        }

        return MessageResponse.builder().message("Profile updated successfully.").build();
    }
}
