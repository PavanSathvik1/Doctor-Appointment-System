package com.hms.service.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for sending email notifications using Thymeleaf templates.
 * <p>
 * All email methods are asynchronous ({@code @Async}) to avoid blocking
 * the request thread. Uses Mailtrap SMTP in development.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@hms.com}")
    private String fromEmail;

    /**
     * Sends an OTP verification email during registration.
     */
    @Async
    public void sendOtpEmail(String toEmail, String firstName, String otp) {
        Context context = new Context();
        context.setVariable("firstName", (firstName != null && !firstName.isBlank()) ? firstName : "User");
        context.setVariable("otp", otp);
        context.setVariable("validMinutes", 5);

        String subject = "HMS - Verify Your Email Address";
        sendTemplatedEmail(toEmail, subject, "email/otp-email", context);
    }

    /**
     * Sends a welcome email after successful account activation.
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        Context context = new Context();
        context.setVariable("firstName", (firstName != null && !firstName.isBlank()) ? firstName : "User");

        String subject = "Welcome to HMS - Account Activated";
        sendTemplatedEmail(toEmail, subject, "email/welcome-email", context);
    }

    /**
     * Sends a password reset email with a link containing the reset token.
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        Context context = new Context();
        context.setVariable("resetToken", resetToken);
        context.setVariable("resetLink", "http://localhost:3000/reset-password?token=" + resetToken);
        context.setVariable("validMinutes", 15);

        String subject = "HMS - Password Reset Request";
        sendTemplatedEmail(toEmail, subject, "email/password-reset", context);
    }

    /**
     * Sends a doctor application approval notification.
     */
    @Async
    public void sendDoctorApprovedEmail(String toEmail, String doctorName) {
        Context context = new Context();
        context.setVariable("doctorName", (doctorName != null && !doctorName.isBlank()) ? doctorName : "Doctor");

        String subject = "HMS - Your Doctor Application Has Been Approved";
        sendTemplatedEmail(toEmail, subject, "email/doctor-approved", context);
    }

    /**
     * Sends a doctor application rejection notification with reason.
     */
    @Async
    public void sendDoctorRejectedEmail(String toEmail, String doctorName, String reason) {
        Context context = new Context();
        context.setVariable("doctorName", (doctorName != null && !doctorName.isBlank()) ? doctorName : "Doctor");
        context.setVariable("reason", reason);

        String subject = "HMS - Doctor Application Update";
        sendTemplatedEmail(toEmail, subject, "email/doctor-rejected", context);
    }

    /**
     * Sends appointment booking confirmation to a recipient.
     */
    @Async
    public void sendAppointmentBookedEmail(String toEmail, String recipientName,
                                            String doctorName, String patientName,
                                            String appointmentDate) {
        sendAppointmentBookedEmailSync(toEmail, recipientName, doctorName, patientName, appointmentDate);
    }

    /** Non-async version for use inside sendSequential. */
    public void sendAppointmentBookedEmailSync(String toEmail, String recipientName,
                                               String doctorName, String patientName,
                                               String appointmentDate) {
        Context context = new Context();
        context.setVariable("recipientName", (recipientName != null && !recipientName.isBlank()) ? recipientName : "User");
        context.setVariable("doctorName", (doctorName != null && !doctorName.isBlank()) ? doctorName : "Doctor");
        context.setVariable("patientName", (patientName != null && !patientName.isBlank()) ? patientName : "Patient");
        context.setVariable("appointmentDate", appointmentDate);

        String subject = "HMS - Appointment Confirmed";
        sendTemplatedEmail(toEmail, subject, "email/appointment-booked", context);
    }

    /**
     * Sends appointment cancellation notification.
     */
    @Async
    public void sendAppointmentCancelledEmail(String toEmail, String recipientName,
                                               String appointmentDate, String reason) {
        Context context = new Context();
        context.setVariable("recipientName", (recipientName != null && !recipientName.isBlank()) ? recipientName : "User");
        context.setVariable("appointmentDate", appointmentDate);
        context.setVariable("reason", reason);

        String subject = "HMS - Appointment Cancelled";
        sendTemplatedEmail(toEmail, subject, "email/appointment-cancelled", context);
    }

    /**
     * Sends appointment confirmation with optional Jitsi meeting link for ONLINE appointments.
     */
    @Async
    public void sendAppointmentConfirmedEmail(String toEmail, String recipientName,
                                               String doctorName, String patientName,
                                               String appointmentDate, String meetingLink, String clinicAddress) {
        sendAppointmentConfirmedEmailSync(toEmail, recipientName, doctorName, patientName, appointmentDate, meetingLink, clinicAddress);
    }

    /** Non-async version for use inside sendSequential. */
    public void sendAppointmentConfirmedEmailSync(String toEmail, String recipientName,
                                                   String doctorName, String patientName,
                                                   String appointmentDate, String meetingLink, String clinicAddress) {
        Context context = new Context();
        context.setVariable("recipientName", (recipientName != null && !recipientName.isBlank()) ? recipientName : "User");
        context.setVariable("doctorName", (doctorName != null && !doctorName.isBlank()) ? doctorName : "Doctor");
        context.setVariable("patientName", (patientName != null && !patientName.isBlank()) ? patientName : "Patient");
        context.setVariable("appointmentDate", appointmentDate);
        context.setVariable("meetingLink", meetingLink);
        context.setVariable("isOnline", meetingLink != null && !meetingLink.isBlank());
        context.setVariable("clinicAddress", clinicAddress != null ? clinicAddress : "Please contact the clinic for address details.");

        String subject = meetingLink != null ? "HMS - Appointment Confirmed 💻 Video Link Inside" : "HMS - Appointment Confirmed";
        sendTemplatedEmail(toEmail, subject, "email/appointment-confirmed", context);
    }

    /**
     * Sends a 24-hour appointment reminder.
     */
    @Async
    public void sendAppointmentReminderEmail(String toEmail, String patientName,
                                              String doctorName, String appointmentDate) {
        Context context = new Context();
        context.setVariable("patientName", (patientName != null && !patientName.isBlank()) ? patientName : "Patient");
        context.setVariable("doctorName", (doctorName != null && !doctorName.isBlank()) ? doctorName : "Doctor");
        context.setVariable("appointmentDate", appointmentDate);

        String subject = "HMS - Appointment Reminder (Tomorrow)";
        sendTemplatedEmail(toEmail, subject, "email/appointment-reminder", context);
    }

    /**
     * Sends a notification when a prescription has been issued.
     */
    @Async
    public void sendPrescriptionIssuedEmail(String toEmail, String patientName,
                                              String doctorName, Long prescriptionId) {
        Context context = new Context();
        context.setVariable("patientName", (patientName != null && !patientName.isBlank()) ? patientName : "Patient");
        context.setVariable("doctorName", (doctorName != null && !doctorName.isBlank()) ? doctorName : "Doctor");
        context.setVariable("prescriptionLink",
                "http://localhost:3000/prescriptions/" + prescriptionId);

        String subject = "HMS - New Prescription Available";
        sendTemplatedEmail(toEmail, subject, "email/prescription-issued", context);
    }

    /**
     * Sends a payment success receipt email to the patient.
     */
    @Async
    public void sendPaymentSuccessEmail(String toEmail, String patientName,
                                         String doctorName, String appointmentDate,
                                         String paymentId, java.math.BigDecimal amount) {
        Context context = new Context();
        context.setVariable("patientName", (patientName != null && !patientName.isBlank()) ? patientName : "Patient");
        context.setVariable("doctorName", (doctorName != null && !doctorName.isBlank()) ? doctorName : "Doctor");
        context.setVariable("appointmentDate", appointmentDate);
        context.setVariable("paymentId", paymentId);
        context.setVariable("amount", amount != null ? amount.toPlainString() : "0");

        String subject = "HMS - Payment Successful ✅ Booking Confirmed";
        sendTemplatedEmail(toEmail, subject, "email/payment-success", context);
    }

    /**
     * Sends multiple emails sequentially with a 2.5s gap between each
     * to stay within Mailtrap free-tier rate limits (1 email/second).
     */
    @Async
    public void sendSequential(List<Runnable> emailTasks) {
        for (int i = 0; i < emailTasks.size(); i++) {
            if (i > 0) {
                try { Thread.sleep(2500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            emailTasks.get(i).run();
        }
    }

    // ─── Private Helper ────────────────────────────────────────────

    /**
     * Renders a Thymeleaf email template and sends it as an HTML email.
     *
     * @param to           the recipient email address
     * @param subject      the email subject line
     * @param templateName the Thymeleaf template name (without .html extension)
     * @param context      the Thymeleaf context with template variables
     */
    private void sendTemplatedEmail(String to, String subject, String templateName, Context context) {
        try {
            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email: to={}, subject={}, error={}", to, subject, e.getMessage(), e);
        }
    }
}
