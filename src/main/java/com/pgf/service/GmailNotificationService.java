package com.pgf.service;

import com.pgf.dto.ContactMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.notify-email}")
    private String adminNotifyEmail;

    @Value("${app.contact.notify-email}")
    private String contactNotifyEmail;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendApprovalConfirmation(String email, String displayName) {
        String body = "<p>Bonjour <strong>" + displayName + "</strong>,</p>" +
                "<p>Votre demande d'accès administrateur sur le site PGF a bien été approuvée.</p>" +
                "<p style='text-align:center;margin:2rem 0;'>" +
                "<a href='" + frontendUrl + "/admin/login' " + ctaStyle() + ">Accéder au panel administrateur</a></p>";
        send(email, "Accès administrateur - Site PGF", wrap(body));
    }

    public void sendAdminApprovalRequest(String userId, String email, String displayName) {
        String approveUrl = backendUrl + "/api/admin/auth/approve/" + userId;
        String body = "<p>Une nouvelle demande d'accès administrateur a été soumise.</p>" +
                infoTable(new String[][]{{"Nom", displayName}, {"Email", email}}) +
                "<p style='text-align:center;margin:2rem 0;'>" +
                "<a href='" + approveUrl + "' " + ctaStyle() + ">Approuver cet utilisateur</a></p>";
        send(adminNotifyEmail, "Nouvelle demande d'accès admin — PGF", wrap(body));
    }

    public void sendInvitation(String email, String token) {
        String registerUrl = frontendUrl + "/admin/register?token=" + token;
        String body = "<p>Vous avez été invité(e) à rejoindre l'équipe d'administration du site PGF.</p>" +
                "<p>Cliquez sur le bouton ci-dessous pour créer votre compte. Ce lien est à usage unique.</p>" +
                "<p style='text-align:center;margin:2rem 0;'>" +
                "<a href='" + registerUrl + "' " + ctaStyle() + ">Créer mon compte administrateur</a></p>";
        send(email, "Invitation à l'administration du site PGF", wrap(body));
    }

    public void sendContactNotification(ContactMessageDto dto) {
        String body = "<p>Un nouveau message de contact a été reçu via le site PGF.</p>" +
                infoTable(new String[][]{
                        {"Nom", dto.getName()},
                        {"Email", "<a href='mailto:" + dto.getEmail() + "' style='color:#706969;'>" + dto.getEmail() + "</a>"},
                        {"Sujet", dto.getSubject() != null ? dto.getSubject() : "—"},
                        {"Téléphone", dto.getPhone() != null ? dto.getPhone() : "—"}
                }) +
                "<div style='margin-top:1.5rem;padding:1rem 1.25rem;background:#f9f9f9;border-left:3px solid #706969;border-radius:4px;'>" +
                "<p style='margin:0;font-size:14px;color:#444;white-space:pre-wrap;'>" + dto.getMessage() + "</p></div>";
        send(contactNotifyEmail.split(","), "Nouveau message de contact — PGF", wrap(body));
    }

    private String wrap(String body) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='margin:0;padding:0;background:#f4f4f4;font-family:Georgia,serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f4f4;padding:40px 0;'><tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);'>" +
                "<tr><td style='background:#2c2c2c;padding:24px 40px;text-align:center;'>" +
                "<span style='color:#ffffff;font-size:20px;letter-spacing:0.15em;font-family:Georgia,serif;'>PGF</span></td></tr>" +
                "<tr><td style='padding:36px 40px;color:#333333;font-size:15px;line-height:1.7;'>" +
                body +
                "</td></tr>" +
                "<tr><td style='background:#f9f9f9;padding:20px 40px;text-align:center;border-top:1px solid #eeeeee;'>" +
                "<p style='margin:0;font-size:12px;color:#999999;'>© PGF — Pierrette Gonseth Favre</p></td></tr>" +
                "</table></td></tr></table></body></html>";
    }

    private String infoTable(String[][] rows) {
        StringBuilder sb = new StringBuilder("<table cellpadding='0' cellspacing='0' style='width:100%;margin-top:1.5rem;border-collapse:collapse;font-size:14px;'>");
        for (String[] row : rows) {
            sb.append("<tr>")
                    .append("<td style='padding:10px 12px;font-weight:bold;color:#555;background:#f9f9f9;width:120px;border-bottom:1px solid #eee;'>").append(row[0]).append("</td>")
                    .append("<td style='padding:10px 12px;color:#333;background:#fff;border-bottom:1px solid #eee;'>").append(row[1]).append("</td>")
                    .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String ctaStyle() {
        return "style='display:inline-block;padding:12px 28px;background:#706969;color:#ffffff;text-decoration:none;" +
                "border-radius:4px;font-size:14px;letter-spacing:0.05em;font-family:Georgia,serif;'";
    }

    private void send(String to, String subject, String html) {
        send(new String[]{to}, subject, html);
    }

    private void send(String[] to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {}", (Object) to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}