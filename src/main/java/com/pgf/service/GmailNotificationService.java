package com.pgf.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.notify-email}")
    private String notifyEmail;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendApprovalConfirmation(String email, String displayName) {
        String html = "<p>Bonjour " + displayName + ",</p>" +
                "<p>Votre demande d'accès administrateur sur le site PGF a bien été approuvée.</p>" +
                "<p><a href=\"" + frontendUrl + "/admin/login\">Accéder au panel administrateur</a></p>";
        send(email, "Votre demande d'accès admin a été approuvée", html);
    }

    public void sendAdminApprovalRequest(String userId, String email, String displayName) {
        String approveUrl = backendUrl + "/api/admin/auth/approve/" + userId;
        String html = "<p>Nouvelle demande d'accès admin :</p>" +
                "<ul><li><b>Nom</b> : " + displayName + "</li>" +
                "<li><b>Email</b> : " + email + "</li></ul>" +
                "<p><a href=\"" + approveUrl + "\">Approuver cet utilisateur</a></p>";
        send(notifyEmail, "Nouvelle demande d'accès admin PGF", html);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public void sendInvitation(String email, String token) {
        String registerUrl = frontendUrl + "/admin/register?token=" + token;
        String html = "<p>Vous avez été invité(e) à rejoindre l'administration du site PGF.</p>" +
                "<p><a href=\"" + registerUrl + "\">Créer mon compte administrateur</a></p>" +
                "<p>Ce lien est à usage unique.</p>";
        send(email, "Invitation à l'administration du site PGF", html);
    }
}