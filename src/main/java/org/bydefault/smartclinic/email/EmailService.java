package org.bydefault.smartclinic.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.entities.ApplicationStatus;
import org.bydefault.smartclinic.entities.Doctor;
import org.bydefault.smartclinic.entities.User;
import org.bydefault.smartclinic.exception.EmailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Value("${app.name:Smart Clinic}")
    private String appName;


    public void appointmentReminderEmail(String to, User user) {
    }

    public void sendAppointmentCanceledEmail(String to, User user) {
    }

    public void sendAppointmentConfirmedEmail(String to, User user) {
    }

    public void sendDoctorAcceptedEmail(String to, User user) {

    }

    public void sendDoctorRejectedEmail(String to, User user) {

    }

    public void sendDoctorApplicationEmail(String to, Doctor application, ApplicationStatus status) {
        Context context = new Context();

        context.setVariable("full_name", application.getFullName() != null ? application.getFullName() : "User");
        context.setVariable("app_name", appName);
        context.setVariable("application_status", status.name());

        // Set status-specific variables
        setStatusSpecificVariables(context, status, application);

        String htmlContent = templateEngine.process("doctor-application-email", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject(getSubjectByStatus(status));
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            log.info("Doctor application email sent successfully to: {} for status: {}", to, status);

        } catch (Exception e) {
            log.error("Failed to send doctor application email to: {} for status: {}", to, status, e);
            throw new EmailException("Failed to send doctor application email: " + e.getMessage());
        }
    }

    private void setStatusSpecificVariables(Context context, ApplicationStatus status, Doctor application) {
        switch (status) {
            case PENDING:
                context.setVariable("status_title", "Application Received");
                context.setVariable("status_message", "We have successfully received your doctor application.");
                context.setVariable("status_description", "Our team will review your application and get back to you within 3-5 business days.");
                context.setVariable("status_color", "#007bff"); // Blue
                context.setVariable("show_next_steps", true);
                context.setVariable("next_steps", getNextStepsForPending());
                break;

            case APPROVED:
                context.setVariable("status_title", "Application Approved! 🎉");
                context.setVariable("status_message", "Congratulations! Your doctor application has been approved.");
                context.setVariable("status_description", "You can now start providing medical consultations on our platform.");
                context.setVariable("status_color", "#28a745"); // Green
                context.setVariable("show_next_steps", true);
                context.setVariable("next_steps", getNextStepsForApproved());
                context.setVariable("login_url", "http://localhost:9090/api/v1/auth/login/");
                break;

            case REJECTED:
                context.setVariable("status_title", "Application Update");
                context.setVariable("status_message", "Thank you for your interest in becoming a doctor on our platform.");
                context.setVariable("status_description", "After careful review, we are unable to approve your application at this time.");
                context.setVariable("status_color", "#dc3545"); // Red
                context.setVariable("show_next_steps", true);
                context.setVariable("next_steps", getNextStepsForRejected());
                break;

            case UNDER_REVIEW:
                context.setVariable("status_title", "Application Under Review");
                context.setVariable("status_message", "Your doctor application is currently under review.");
                context.setVariable("status_description", "Our medical team is carefully reviewing your credentials and qualifications.");
                context.setVariable("status_color", "#ffc107"); // Yellow
                context.setVariable("show_next_steps", false);
                break;
        }

        // Common variables
        context.setVariable("application_date", application.getSubmittedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        context.setVariable("support_email", "support@" + appName.toLowerCase() + ".com");
        context.setVariable("current_year", LocalDateTime.now().getYear());
    }

    private String getSubjectByStatus(ApplicationStatus status) {
        String baseSubject = "Doctor Application";
        switch (status) {
            case PENDING:
                return baseSubject + " Received - " + appName;
            case APPROVED:
                return baseSubject + " Approved - Welcome to " + appName;
            case REJECTED:
                return baseSubject + " Update - " + appName;
            case UNDER_REVIEW:
                return baseSubject + " Under Review - " + appName;
            default:
                return baseSubject + " Update - " + appName;
        }
    }

    private List<String> getNextStepsForPending() {
        return Arrays.asList(
                "Our medical team will verify your credentials",
                "You will receive an email update within 3-5 business days",
                "Keep an eye on your email for further instructions"
        );
    }

    private List<String> getNextStepsForApproved() {
        return Arrays.asList(
                "Complete your doctor profile setup",
                "Set your availability and consultation hours",
                "Start accepting patient consultations",
                "Access your doctor dashboard to manage appointments"
        );
    }

    private List<String> getNextStepsForRejected() {
        return Arrays.asList(
                "Review the feedback provided below",
                "You may reapply after addressing the mentioned requirements",
                "Contact our support team if you have questions",
                "Consider additional certifications if needed"
        );
    }

    public void sendVerificationEmail(String to, User user) {
        Context context = new Context();
        context.setVariable("full_name", user.getFullName() != null ? user.getFullName() : "User");
        context.setVariable("verification_code", user.getCode());
        context.setVariable("app_name", appName);
        context.setVariable("expiry_minutes", 15);

        String htmlContent = templateEngine.process("verification-email", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(htmlContent, true);
            helper.setTo(to);
            helper.setSubject("Verify Your Account - " + appName);
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            log.info("Verification email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
            throw new EmailException("Failed to send verification email" + e);
        }
    }
}



