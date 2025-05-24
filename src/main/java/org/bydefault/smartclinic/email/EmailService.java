package org.bydefault.smartclinic.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.entities.User;
import org.bydefault.smartclinic.exception.EmailException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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

    public void sendResetPasswordEmail(String to, String code) {
    }

    public void  appointmentReminderEmail(String to, User user) {
    }

    public void sendAppointmentCanceledEmail(String to, User user) {}

    public void sendAppointmentConfirmedEmail(String to, User user) {}

    public void  sendDoctorAcceptedEmail(String to, User user) {}


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
