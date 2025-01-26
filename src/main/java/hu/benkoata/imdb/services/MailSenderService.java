package hu.benkoata.imdb.services;

import hu.benkoata.imdb.exceptions.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
//Todo mockolt mailSender segítségével tesztelni a funkciókat
public class MailSenderService {
    private final JavaMailSender mailSender;
    private final String from;
    private final MessageSource messageSource;
    private final Object[] emptyArgs = new Object[]{};

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(from);
        mailSender.send(message);
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setText(htmlContent, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom(from);
        mailSender.send(mimeMessage);
    }

    public void sendEmailValidification(HttpServletRequest request,
                                        long userId,
                                        int verificationCode,
                                        String email,
                                        String endpoint,
                                        Locale locale) {
        String url = getVerificationUrl(request, userId, verificationCode, endpoint);
        String html = getValidificationHtmlContent(
                locale,
                messageSource.getMessage("emailValidificationTitle", emptyArgs, locale),
                messageSource.getMessage("emailValidificationText", emptyArgs, locale),
                url);
        try {
            sendHtmlEmail(email, "email validification", html);
        } catch (MessagingException e) {
            throw new EmailException(request.getRequestURI());
        }
    }

    private String getValidificationHtmlContent(Locale locale, String title, String text, String url) {
        return getBaseHtmlContent("validification-template.html", locale, title, text)
                .replace("${alternativeLink}", messageSource.getMessage("validificationAlternativeLink", emptyArgs, locale))
                .replace("${linkToValidification}", url);
    }

    private String getBaseHtmlContent(String templateFilename, Locale locale, String title, String text) {
        String html = new ResourceFileReaderService(
                MailSenderService.class,
                templateFilename)
                .getAsString();
        return html.replace("${lang}", locale.toLanguageTag())
                .replace("${title}", title)
                .replace("${greeting}", messageSource.getMessage("validificationGreeting", emptyArgs, locale))
                .replace("${text}", text);
    }

    //Todo teszt
    String getVerificationUrl(HttpServletRequest request, long userId, int verificationCode, String endpoint) {
        return String.format("%s://%s:%d%s%s",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                request.getContextPath(),
                endpoint.replace("{id}", Long.toString(userId))
                        .replace("{code}", Integer.toString(verificationCode)));
    }

    public void sendUnlockUser(HttpServletRequest request,
                               long userId,
                               int verificationCode,
                               String email,
                               String endpoint,
                               Locale locale) {
        String url = getVerificationUrl(request, userId, verificationCode, endpoint);
        String html = getValidificationHtmlContent(
                locale,
                messageSource.getMessage("unlockUserTitle", emptyArgs, locale),
                messageSource.getMessage("unlockUserText", emptyArgs, locale),
                url);
        try {
            sendHtmlEmail(email, "unlock user", html);
        } catch (MessagingException e) {
            throw new EmailException(request.getRequestURI());
        }
    }

    public void sendDeleteValidification(HttpServletRequest request,
                                         long userId,
                                         int verificationCode,
                                         String email,
                                         String endpoint,
                                         Locale locale) {
        String url = getVerificationUrl(request, userId, verificationCode, endpoint);
        String html = getValidificationHtmlContent(
                locale,
                messageSource.getMessage("deleteValidificationTitle", emptyArgs, locale),
                messageSource.getMessage("deleteValidificationText", emptyArgs, locale),
                url);
        try {
            sendHtmlEmail(email, "deletion validification", html);
        } catch (MessagingException e) {
            throw new EmailException(request.getRequestURI());
        }
    }

    public void sendResetPassword(HttpServletRequest request, int resetPasswordCode, String email, Locale locale) {
        String html = getBaseHtmlContent(
                "reset-password-template.html",
                locale,
                messageSource.getMessage("resetPasswordTitle", emptyArgs, locale),
                messageSource.getMessage("resetPasswordText", new Integer[]{resetPasswordCode}, locale)
        );
        try {
            sendHtmlEmail(email, "reset password", html);
        } catch (MessagingException e) {
            throw new EmailException(request.getRequestURI());
        }
    }
}
