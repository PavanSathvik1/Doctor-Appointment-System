package com.hms.service.appointment;

import com.hms.dto.appointment.request.AppointmentRequest;
import com.hms.dto.appointment.response.AppointmentResponse;
import com.hms.dto.appointment.response.BookingResponse;
import com.hms.dto.appointment.response.TimeSlotResponse;
import com.hms.entity.appointment.Appointment;
import com.hms.entity.appointment.AppointmentStatus;
import com.hms.entity.common.ConsultationMode;
import com.hms.entity.payment.PaymentStatus;
import com.hms.mapper.appointment.AppointmentMapper;
import com.hms.repository.appointment.AppointmentRepository;
import com.hms.dto.auth.response.MessageResponse;
import com.hms.exception.BusinessRuleException;
import com.hms.exception.ConflictException;
import com.hms.exception.ResourceNotFoundException;
import com.hms.entity.doctor.Doctor;
import com.hms.entity.doctor.DoctorAvailability;
import com.hms.repository.doctor.DoctorAvailabilityRepository;
import com.hms.repository.doctor.DoctorRepository;
import com.hms.service.notification.NotificationService;
import com.hms.service.payment.RazorpayService;
import com.hms.dto.payment.PaymentVerifyRequest;
import com.hms.entity.user.User;
import com.hms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AppointmentService resolving dynamic scheduling constraints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentMapper appointmentMapper;
    private final NotificationService notificationService;
    private final RazorpayService razorpayService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return List.of(); // Past dates yield no availability
        }

        // Check if doctor has rules for this day of week
        Optional<DoctorAvailability> ruleOpt = availabilityRepository
                .findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, date.getDayOfWeek());

        if (ruleOpt.isEmpty()) {
            return List.of(); // Not working on this day
        }

        DoctorAvailability rule = ruleOpt.get();
        List<Appointment> existingAppointments = appointmentRepository.findActiveAppointmentsForDoctorOnDate(doctorId, date);

        List<TimeSlotResponse> slots = new ArrayList<>();
        LocalTime currentSlotStart = rule.getStartTime();
        LocalTime closingTime = rule.getEndTime();
        int duration = rule.getSlotDurationMinutes();

        LocalDateTime now = LocalDateTime.now();

        while (currentSlotStart.plusMinutes(duration).isBefore(closingTime) || currentSlotStart.plusMinutes(duration).equals(closingTime)) {
            LocalTime currentSlotEnd = currentSlotStart.plusMinutes(duration);

            // Filter out past slots for today
            boolean isPastSlot = date.equals(now.toLocalDate()) && currentSlotStart.isBefore(now.toLocalTime());

            if (!isPastSlot) {
                // Check against existing appointments
                boolean overlap = false;
                for (Appointment appt : existingAppointments) {
                    // Start is inside an existing appt, OR existing appt is inside the slot
                    if ((currentSlotStart.isAfter(appt.getStartTime()) || currentSlotStart.equals(appt.getStartTime())) 
                        && currentSlotStart.isBefore(appt.getEndTime())) {
                        overlap = true;
                        break;
                    }
                    if ((currentSlotEnd.isAfter(appt.getStartTime())) 
                        && (currentSlotEnd.isBefore(appt.getEndTime()) || currentSlotEnd.equals(appt.getEndTime()))) {
                        overlap = true;
                        break;
                    }
                }

                slots.add(TimeSlotResponse.builder()
                        .startTime(currentSlotStart)
                        .endTime(currentSlotEnd)
                        .isAvailable(!overlap)
                        .build());
            }
            currentSlotStart = currentSlotEnd;
        }

        return slots;
    }

    @Override
    @Transactional
    public BookingResponse bookAppointment(Long patientId, AppointmentRequest request) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId.toString()));
                
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", request.getDoctorId().toString()));

        // Fetch the working rule to determine the slot's expected end time
        DoctorAvailability rule = availabilityRepository
                .findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctor.getId(), request.getAppointmentDate().getDayOfWeek())
                .orElseThrow(() -> new BusinessRuleException("Doctor is not available on this day."));

        LocalTime reqStart = request.getStartTime();
        LocalTime reqEnd = reqStart.plusMinutes(rule.getSlotDurationMinutes());

        if (reqStart.isBefore(rule.getStartTime()) || reqEnd.isAfter(rule.getEndTime())) {
            throw new BusinessRuleException("Requested time is outside doctor's working hours.");
        }

        // DB level overlap check (thread-safe fallback)
        boolean hasOverlap = appointmentRepository.existsOverlappingAppointment(
                doctor.getId(), request.getAppointmentDate(), reqStart, reqEnd);

        if (hasOverlap) {
            throw new ConflictException("The requested time slot is already booked. Please refresh and select another.");
        }

        if (doctor.getMode() != request.getMode()) {
            throw new BusinessRuleException("Doctor only accepts " + doctor.getMode() + " appointments.");
        }

        String razorpayOrderId = null;
        if (doctor.getConsultationFee() != null && doctor.getConsultationFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
            String receipt = "receipt_" + System.currentTimeMillis();
            razorpayOrderId = razorpayService.createOrder(doctor.getConsultationFee(), receipt);
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(reqStart)
                .endTime(reqEnd)
                .reasonForVisit(request.getReasonForVisit())
                .status(AppointmentStatus.PENDING) // Awaiting doc confirmation in phase 3
                .mode(request.getMode())
                .paymentStatus(PaymentStatus.PENDING)
                .razorpayOrderId(razorpayOrderId)
                .build();

        appointmentRepository.save(appointment);

        // Notify patient & doctor
        String formattedDate = LocalDateTime.of(request.getAppointmentDate(), reqStart).format(DATE_TIME_FORMATTER);
        // Use user's actual name from the User entity (set during registration)
        User patientUser = patient;
        String patientFirstName = (patientUser.getFirstName() != null && !patientUser.getFirstName().isBlank()) 
                ? patientUser.getFirstName() : patientUser.getEmail().split("@")[0];
        String patientLastName = (patientUser.getLastName() != null && !patientUser.getLastName().isBlank()) 
                ? patientUser.getLastName() : "";
        String patientFullName = (patientLastName.isBlank()) ? patientFirstName : patientFirstName + " " + patientLastName;
        
        User docUser = doctor.getUser();
        String docFirstName = (docUser.getFirstName() != null) ? docUser.getFirstName() : "";
        String docLastName = (docUser.getLastName() != null) ? docUser.getLastName() : "Doctor";
        String docFullName = "Dr. " + docFirstName + " " + docLastName;

        // Send sequentially to respect Mailtrap free-tier (1 email/sec)
        final String fPatientEmail = patientUser.getEmail();
        final String fPatientFirst = patientFirstName;
        final String fDocEmail = docUser.getEmail();
        final String fDocFull = docFullName;
        final String fPatientFull = patientFullName;
        final String fDate = formattedDate;
        notificationService.sendSequential(java.util.List.of(
            () -> notificationService.sendAppointmentBookedEmailSync(fPatientEmail, fPatientFirst, fDocFull, fPatientFull, fDate),
            () -> notificationService.sendAppointmentBookedEmailSync(fDocEmail, fDocFull, fDocFull, fPatientFull, fDate)
        ));

        return BookingResponse.builder()
                .message("Appointment successfully booked!")
                .razorpayOrderId(razorpayOrderId)
                .appointmentId(appointment.getId())
                .build();
    }

    @Override
    @Transactional
    public MessageResponse verifyPayment(Long appointmentId, PaymentVerifyRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId.toString()));

        if (appointment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new BusinessRuleException("Payment already verified for this appointment.");
        }

        boolean isValid = razorpayService.verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            appointment.setPaymentStatus(PaymentStatus.FAILED);
            appointmentRepository.save(appointment);
            throw new BusinessRuleException("Payment verification failed. Invalid signature.");
        }

        appointment.setPaymentStatus(PaymentStatus.COMPLETED);
        appointment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        appointmentRepository.save(appointment);

        // Send payment success email to patient
        User pUser = appointment.getPatient();
        String pFirstName = (pUser.getFirstName() != null && !pUser.getFirstName().isBlank())
                ? pUser.getFirstName() : pUser.getEmail().split("@")[0];
        User dUser = appointment.getDoctor().getUser();
        String dName = "Dr. " + (dUser.getFirstName() != null ? dUser.getFirstName() : "") 
                + " " + (dUser.getLastName() != null ? dUser.getLastName() : "Doctor");
        String apptDate = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime())
                .format(DATE_TIME_FORMATTER);
        notificationService.sendPaymentSuccessEmail(
                pUser.getEmail(), pFirstName, dName, apptDate,
                request.getRazorpayPaymentId(), appointment.getDoctor().getConsultationFee());

        return MessageResponse.builder().message("Payment verified successfully.").build();
    }

    @Override
    @Transactional
    public MessageResponse updateAppointmentStatus(Long appointmentId, String newStatusStr, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId.toString()));

        AppointmentStatus newStatus;
        try {
            newStatus = AppointmentStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid status update.");
        }

        if (newStatus == AppointmentStatus.CANCELLED && (reason == null || reason.isBlank())) {
            throw new BusinessRuleException("A reason must be provided when cancelling an appointment.");
        }

        appointment.setStatus(newStatus);
        if (newStatus == AppointmentStatus.CANCELLED) {
            appointment.setCancellationReason(reason);
            
            // Notify cancellation
            String formattedDate = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime()).format(DATE_TIME_FORMATTER);
            String patientName = appointment.getPatient().getFirstName();
            User apptDocUser = appointment.getDoctor().getUser();
            String apptDocLastName = apptDocUser.getLastName() != null ? apptDocUser.getLastName() : "Doctor";
            notificationService.sendAppointmentCancelledEmail(appointment.getPatient().getEmail(), patientName, formattedDate, reason);
            notificationService.sendAppointmentCancelledEmail(apptDocUser.getEmail(), "Dr. " + apptDocLastName, formattedDate, reason);
        } else if (newStatus == AppointmentStatus.CONFIRMED) {
            String formattedDate = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getStartTime()).format(DATE_TIME_FORMATTER);
            User confPatientUser = appointment.getPatient();
            String patientFirstName = (confPatientUser.getFirstName() != null && !confPatientUser.getFirstName().isBlank())
                    ? confPatientUser.getFirstName() : confPatientUser.getEmail().split("@")[0];
            String patientLastName = (confPatientUser.getLastName() != null && !confPatientUser.getLastName().isBlank())
                    ? confPatientUser.getLastName() : "";
            String patientFullName = patientLastName.isBlank() ? patientFirstName : patientFirstName + " " + patientLastName;
            User confDocUser = appointment.getDoctor().getUser();
            String confDocFirstName = confDocUser.getFirstName() != null ? confDocUser.getFirstName() : "";
            String confDocLastName = confDocUser.getLastName() != null ? confDocUser.getLastName() : "Doctor";
            String docFullName = "Dr. " + confDocFirstName + " " + confDocLastName;

            String meetingLink = null;
            String clinicAddress = null;
            if (appointment.getMode() == ConsultationMode.ONLINE) {
                String roomId = "hms-" + java.util.UUID.randomUUID().toString().substring(0, 8);
                meetingLink = "https://meet.jit.si/" + roomId;
                appointment.setMeetingLink(meetingLink);
            } else {
                // Use doctor's registered clinic address, fallback to generic
                clinicAddress = (appointment.getDoctor().getClinicAddress() != null
                        && !appointment.getDoctor().getClinicAddress().isBlank())
                        ? appointment.getDoctor().getClinicAddress()
                        : "Please contact the clinic for address details.";
                String hospitalName = (appointment.getDoctor().getHospitalName() != null
                        && !appointment.getDoctor().getHospitalName().isBlank())
                        ? appointment.getDoctor().getHospitalName() : "";
                String fullAddress = hospitalName.isBlank() ? clinicAddress : hospitalName + ", " + clinicAddress;
                appointment.setClinicAddress(fullAddress);
                clinicAddress = fullAddress;
            }

            // Send sequentially to respect Mailtrap free-tier (1 email/sec)
            final String fPEmail = confPatientUser.getEmail();
            final String fPFirst = patientFirstName;
            final String fDEmail = confDocUser.getEmail();
            final String fDFull = docFullName;
            final String fPFull = patientFullName;
            final String fDate2 = formattedDate;
            final String fLink = meetingLink;
            final String fAddress = clinicAddress;
            notificationService.sendSequential(java.util.List.of(
                () -> notificationService.sendAppointmentConfirmedEmailSync(fPEmail, fPFirst, fDFull, fPFull, fDate2, fLink, fAddress),
                () -> notificationService.sendAppointmentConfirmedEmailSync(fDEmail, fDFull, fDFull, fPFull, fDate2, fLink, fAddress)
            ));
        }

        appointmentRepository.save(appointment);
        return MessageResponse.builder().message("Appointment status updated to " + newStatus.name()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getPatientAppointments(Long patientId, int page, int size) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId, PageRequest.of(page, size))
                .map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(Long doctorId, int page, int size) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctorId, PageRequest.of(page, size))
                .map(appointmentMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorDashboardUpcoming(Long doctorId, int page, int size) {
        return appointmentRepository.findUpcomingByDoctorId(doctorId, PageRequest.of(page, size))
                .map(appointmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(int page, int size) {
        return appointmentRepository.findAll(PageRequest.of(page, size, Sort.by("appointmentDate").descending()))
                .map(appointmentMapper::toResponse);
    }
}
