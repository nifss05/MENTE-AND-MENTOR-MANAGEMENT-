package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendResetEmail(String to, String token) {
        if (mailSender == null) {
            System.out.println("⚠️ Email service not configured. Reset token: " + token);
            System.out.println("📧 To: " + to);
            System.out.println("🔗 Reset link: http://localhost:8080/reset.html?token=" + token);
            return;
        }

        String link = "http://localhost:8080/reset.html?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("Hello,\n\n" +
                "You requested to reset your password. Click the link below to reset it:\n\n" +
                link + "\n\n" +
                "This link will expire in 10 minutes.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\nStudent Academic Record System");

        try {
            mailSender.send(message);
            System.out.println("✅ Reset email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send reset email: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
