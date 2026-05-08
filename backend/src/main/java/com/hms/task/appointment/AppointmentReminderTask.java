package com.hms.task.appointment;

import com.hms.entity.appointment.Appointment;
import com.hms.entity.appointment.AppointmentStatus;
import com.hms.repository.appointment.AppointmentRepository;
import com.hms.service.notification.NotificationService;
import com.hms.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Background task to send appointment reminders to patients.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderTask {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

    /**
     * Sends reminders for appointments scheduled for tomorrow.
     * 
     * Runs once a day at 9:00 AM.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendAppointmentReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("[AppointmentReminderTask] Checking for appointments on: {}", tomorrow);

        List<Appointment> upcomingAppointments = appointmentRepository
                .findByStatusAndAppointmentDate(AppointmentStatus.CONFIRMED, tomorrow);

        if (upcomingAppointments.isEmpty()) {
            log.info("[AppointmentReminderTask] No confirmed appointments found for tomorrow.");
            return;
        }

        log.info("[AppointmentReminderTask] Found {} appointments. Sending reminders...", upcomingAppointments.size());

        for (Appointment appt : upcomingAppointments) {
            try {
                String formattedDate = LocalDateTime.of(appt.getAppointmentDate(), appt.getStartTime()).format(DATE_TIME_FORMATTER);
                String patientName = appt.getPatient().getFirstName() != null ? appt.getPatient().getFirstName() : "Valued Patient";
                
                User docUser = appt.getDoctor().getUser();
                String docLastName = docUser.getLastName() != null ? docUser.getLastName() : "Doctor";
                String doctorName = "Dr. " + docLastName;

                notificationService.sendAppointmentReminderEmail(
                        appt.getPatient().getEmail(),
                        patientName,
                        doctorName,
                        formattedDate
                );
            } catch (Exception e) {
                log.error("[AppointmentReminderTask] Failed to send reminder for appointment ID {}: {}", appt.getId(), e.getMessage());
            }
        }

        log.info("[AppointmentReminderTask] Finished sending {} reminders.", upcomingAppointments.size());
    }
}
