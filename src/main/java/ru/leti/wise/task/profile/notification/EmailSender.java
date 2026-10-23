package ru.leti.wise.task.profile.notification;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
// TODO async send
public class EmailSender {

    @Value("${spring.mail.host}")
    private String smtpHost;

    @Value("${spring.mail.port}")
    private int smtpPort;

    @Value("${spring.mail.from-address}")
    private String fromAddress;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean starttlsEnable;

    public void sendEmail(String toAddress, String subject, String htmlContent) {
        log.debug("Sending email: to='{}', subject='{}', htmlSize={} bytes, smtpHost={}, smtpPort={}",
                toAddress, subject, htmlContent == null ? 0 : htmlContent.length(), smtpHost, smtpPort);
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));

        Session session = Session.getInstance(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            log.info("Email sent successfully: to='{}', subject='{}'", toAddress, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email: to='{}', subject='{}', reason={}",
                    toAddress, subject, e.getMessage(), e);
        }
    }
}