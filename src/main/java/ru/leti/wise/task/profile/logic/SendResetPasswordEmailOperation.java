package ru.leti.wise.task.profile.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import ru.leti.wise.task.profile.ProfileGrpc.SendResetPasswordEmailRequest;
import ru.leti.wise.task.profile.model.PasswordRecoveryEntity;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.notification.EmailSender;
import ru.leti.wise.task.profile.notification.TemplateLoader;
import ru.leti.wise.task.profile.repository.PasswordRecoveryRepository;
import ru.leti.wise.task.profile.validation.ProfileValidator;
import ru.leti.wise.task.profile.validation.RateLimitValidator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendResetPasswordEmailOperation {

    private final TemplateLoader templateLoader;
    private final EmailSender emailSender;
    private final ProfileValidator profileValidator;
    private final RateLimitValidator rateLimitValidator;
    private final PasswordRecoveryRepository passwordRecoveryRepository;

    @Value("${reset-password.email-subject}")
    private String emailSubject;
    @Value("${reset-password.recovery-link}")
    private String RECOVERY_LINK;
    @Value("${reset-password.template}")
    private String template;
    @Value("${reset-password.token-expires}")
    Integer tokenExpiresMinutes;

    private String createEmailHtml(String recoveryToken) {
        String recoveryLink = RECOVERY_LINK + recoveryToken;

        Map<String, String> params = Map.of(
                "RECOVERY_LINK", recoveryLink
        );

        return templateLoader.loadTemplateWithParams(template, params);
    }


    public void activate(SendResetPasswordEmailRequest request) {
        log.info("Requesting reset password email: email='{}'", request.getEmail());
        ProfileEntity profile = profileValidator.checkEmailExistence(request.getEmail());
        rateLimitValidator.checkRateLimit(request.getEmail());
        UUID recoveryToken = UUID.randomUUID();
        PasswordRecoveryEntity passwordRecoveryEntity = new PasswordRecoveryEntity();
        passwordRecoveryEntity.setEmail(request.getEmail());
        passwordRecoveryEntity.setRecoveryToken(recoveryToken);
        passwordRecoveryEntity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        passwordRecoveryEntity.setExpiresAt(Timestamp.valueOf(LocalDateTime.now().plusMinutes(tokenExpiresMinutes)));
        passwordRecoveryRepository.save(passwordRecoveryEntity);
        log.debug("Password recovery request saved: email='{}', expiresAt={}",
                passwordRecoveryEntity.getEmail(), passwordRecoveryEntity.getExpiresAt());
        emailSender.sendEmail(profile.getEmail(), emailSubject, createEmailHtml(recoveryToken.toString()));
        log.info("Reset password email sent: email='{}'", profile.getEmail());
    }
}
