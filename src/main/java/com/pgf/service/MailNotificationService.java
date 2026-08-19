package com.pgf.service;

import com.pgf.dto.ContactMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import jakarta.mail.internet.MimeMessage;
import java.time.Year;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailNotificationService {

    private static final String VIA_SITE = " (via le site)";
    private static final String FONT = "-apple-system,BlinkMacSystemFont,Roboto,Helvetica,Arial,sans-serif";
    private static final String ACCENT = "#706969";
    private static final String PLACEHOLDER = "<span style='color:#999999;'>—</span>";
    private static final String SWISS_PREFIX = "+41";
    private static final Pattern INTERNATIONAL_PHONE = Pattern.compile("^(\\+\\d{1,4})[\\s./-]+(.+)$");

    private final JavaMailSender mailSender;

    @Value("${app.admin.notify-email}")
    private String adminNotifyEmail;

    @Value("${app.contact.notify-email}")
    private String contactNotifyEmail;

    @Value("${app.contact.notify-bcc}")
    private List<String> contactNotifyBcc;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    public void sendApprovalConfirmation(String email, String displayName) {
        String body = heading("Accès administrateur") +
                paragraph("Bonjour <strong style='font-weight:600;'>" + HtmlUtils.htmlEscape(displayName) + "</strong>,") +
                paragraph("Votre demande d'accès administrateur sur le site PGF a bien été approuvée.") +
                cta(frontendUrl + "/admin/login", "Accéder au panel");
        send(email, "Accès administrateur — Site PGF", wrap(body));
    }

    @Async
    public void sendAdminApprovalRequest(String userId, String email, String displayName) {
        String body = heading("Demande d'accès admin") +
                paragraph("Une nouvelle demande d'accès administrateur a été soumise.") +
                infoTable(new String[][]{
                        {"Nom", HtmlUtils.htmlEscape(displayName)},
                        {"Email", mailtoLink(email)}
                }) +
                cta(backendUrl + "/api/admin/auth/approve/" + userId, "Approuver cet utilisateur");
        send(adminNotifyEmail, "Nouvelle demande d'accès admin — PGF", wrap(body));
    }

    @Async
    public void sendInvitation(String email, String token) {
        String body = heading("Invitation") +
                paragraph("Vous avez été invité(e) à rejoindre l'équipe d'administration du site PGF.") +
                paragraph("Ce lien est à usage unique.") +
                cta(frontendUrl + "/admin/register?token=" + token, "Créer mon compte");
        send(email, "Invitation à l'administration du site PGF", wrap(body));
    }

    @Async
    public void sendContactNotification(ContactMessageDto dto) {
        String name = HtmlUtils.htmlEscape(dto.getName());
        String body = heading("Nouveau message") +
                paragraph("Reçu via le formulaire de contact du site.") +
                infoTable(new String[][]{
                        {"Nom", name},
                        {"Email", mailtoLink(dto.getEmail())},
                        {"Sujet", dto.getSubject() != null ? HtmlUtils.htmlEscape(dto.getSubject()) : PLACEHOLDER},
                        {"Téléphone", telLink(dto.getPhone())}
                }) +
                "<table width='100%' cellpadding='0' cellspacing='0' style='margin-top:28px;'><tr>" +
                "<td style='padding:20px 24px;background:#fafafa;border-left:2px solid " + ACCENT + ";" +
                "font-size:15px;line-height:1.75;color:#666666;white-space:pre-wrap;'>" +
                HtmlUtils.htmlEscape(dto.getMessage()) +
                "</td></tr></table>" +
                "<p style='margin:24px 0 0;font-size:13px;line-height:1.6;color:#999999;'>" +
                "Cliquez sur le bouton <strong style='font-weight:600;color:" + ACCENT + ";'>Répondre</strong> " +
                "à cet email pour écrire à " + name + ".</p>";
        send(contactNotifyEmail, contactNotifyBcc, "Nouveau message de contact — PGF", wrap(body), dto.getName(), dto.getEmail());
    }

    static String formatPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        Matcher matcher = INTERNATIONAL_PHONE.matcher(phone.trim());
        if (!matcher.matches()) {
            return phone.trim();
        }
        String national = matcher.group(2).replaceAll("\\D", "").replaceFirst("^0+", "");
        if (national.isEmpty()) {
            return matcher.group(1);
        }
        return matcher.group(1) + " " + group(matcher.group(1), national);
    }

    private static String group(String prefix, String digits) {
        if (SWISS_PREFIX.equals(prefix) && digits.length() == 9) {
            return digits.replaceFirst("(\\d{2})(\\d{3})(\\d{2})(\\d{2})", "$1 $2 $3 $4");
        }
        return digits.replaceAll("(?<=\\d)(?=(\\d{2})+$)", " ");
    }

    private String telLink(String phone) {
        String formatted = formatPhone(phone);
        if (formatted == null) {
            return PLACEHOLDER;
        }
        return link("tel:" + formatted.replaceAll("[^+\\d]", ""), HtmlUtils.htmlEscape(formatted));
    }

    private String mailtoLink(String email) {
        String safe = HtmlUtils.htmlEscape(email);
        return link("mailto:" + safe, safe);
    }

    private String link(String href, String label) {
        return "<a href='" + href + "' style='color:" + ACCENT + ";text-decoration:none;border-bottom:1px solid #dddddd;'>" + label + "</a>";
    }

    private String heading(String title) {
        return "<h1 style='margin:0 0 20px;font-size:20px;font-weight:300;letter-spacing:0.08em;" +
                "text-transform:uppercase;color:" + ACCENT + ";line-height:1.3;'>" + title + "</h1>";
    }

    private String paragraph(String text) {
        return "<p style='margin:0 0 14px;font-size:15px;line-height:1.75;color:#666666;'>" + text + "</p>";
    }

    private String cta(String href, String label) {
        return "<table cellpadding='0' cellspacing='0' style='margin:32px 0 8px;'><tr>" +
                "<td style='background:" + ACCENT + ";border-radius:2px;'>" +
                "<a href='" + href + "' style='display:inline-block;padding:14px 32px;color:#ffffff;" +
                "text-decoration:none;font-size:13px;letter-spacing:0.1em;text-transform:uppercase;font-family:" + FONT + ";'>" +
                label + "</a></td></tr></table>";
    }

    private String infoTable(String[][] rows) {
        StringBuilder sb = new StringBuilder("<table width='100%' cellpadding='0' cellspacing='0' style='margin-top:28px;border-collapse:collapse;'>");
        for (String[] row : rows) {
            sb.append("<tr>")
                    .append("<td style='padding:13px 16px 13px 0;width:104px;vertical-align:top;font-size:11px;")
                    .append("letter-spacing:0.1em;text-transform:uppercase;color:#999999;border-bottom:1px solid #eeeeee;'>")
                    .append(row[0]).append("</td>")
                    .append("<td style='padding:13px 0;font-size:15px;line-height:1.6;color:#333333;border-bottom:1px solid #eeeeee;'>")
                    .append(row[1]).append("</td>")
                    .append("</tr>");
        }
        return sb.append("</table>").toString();
    }

    private String wrap(String body) {
        return "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
                "<body style='margin:0;padding:0;background:#f5f5f5;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f5f5f5;padding:40px 16px;font-family:" + FONT + ";'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;background:#ffffff;border:1px solid #e0e0e0;'>" +
                "<tr><td style='padding:28px 40px;text-align:center;border-bottom:1px solid #e0e0e0;'>" +
                "<span style='font-size:15px;font-weight:300;letter-spacing:0.22em;text-transform:uppercase;color:" + ACCENT + ";'>" +
                "Pierrette Gonseth-Favre</span></td></tr>" +
                "<tr><td style='padding:40px;'>" + body + "</td></tr>" +
                "<tr><td style='padding:24px 40px;text-align:center;border-top:1px solid #e0e0e0;'>" +
                "<p style='margin:0;font-size:12px;line-height:1.6;color:#999999;'>© " + Year.now().getValue() +
                " Pierrette Gonseth-Favre — <a href='" + frontendUrl + "' style='color:#999999;text-decoration:none;'>site officiel</a></p>" +
                "</td></tr></table></td></tr></table></body></html>";
    }

    private void send(String to, String subject, String html) {
        send(to, List.of(), subject, html, null, null);
    }

    private void send(String to, List<String> bcc, String subject, String html, String senderName, String replyToEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (senderName != null) {
                helper.setFrom(fromEmail, senderName + VIA_SITE);
            } else {
                helper.setFrom(fromEmail, fromName);
            }
            if (replyToEmail != null) {
                helper.setReplyTo(replyToEmail, senderName);
            }
            helper.setTo(to);
            if (!bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(String[]::new));
            }
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {} (bcc: {})", to, bcc);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
