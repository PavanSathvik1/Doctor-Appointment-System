package com.hms.service.admin;

import com.hms.dto.admin.response.AdminStatsResponse;
import com.hms.dto.admin.response.UserListItemResponse;
import com.hms.repository.appointment.AppointmentRepository;
import com.hms.dto.auth.response.MessageResponse;
import com.hms.entity.doctor.ApprovalStatus;
import com.hms.repository.doctor.DoctorRepository;
import com.hms.entity.user.Role;
import com.hms.entity.user.User;
import com.hms.entity.user.UserStatus;
import com.hms.entity.common.ConsultationMode;
import com.hms.dto.admin.response.DailySummaryResponse;
import com.hms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getSystemStats() {
        List<Object[]> summariesRaw = appointmentRepository.getDailySummaries(LocalDate.now());
        List<DailySummaryResponse> dailySummaries = summariesRaw.stream().map(row -> {
            return DailySummaryResponse.builder()
                    .specialisation((String) row[0])
                    .mode((ConsultationMode) row[1])
                    .totalAppointments(((Number) row[2]).longValue())
                    .totalRevenue(row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString()))
                    .build();
        }).collect(Collectors.toList());

        return AdminStatsResponse.builder()
                .totalPatients(userRepository.countByRole(Role.PATIENT))
                .totalDoctors(userRepository.countByRole(Role.DOCTOR))
                .todayAppointments(appointmentRepository.countByAppointmentDate(LocalDate.now()))
                .pendingDoctorRegistrations(doctorRepository.countByApprovalStatus(ApprovalStatus.PENDING))
                .dailySummaries(dailySummaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserListItemResponse> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(user -> UserListItemResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .build());
    }

    @Override
    @Transactional
    public MessageResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Prevent disabling the current admin or similar high-privilege users if needed
        // For Phase 2, we just toggle between ACTIVE and SUSPENDED
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.SUSPENDED);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        
        userRepository.save(user);
        return new MessageResponse("User status updated successfully to " + user.getStatus());
    }
}
