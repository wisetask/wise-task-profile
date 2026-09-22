package ru.leti.wise.task.profile.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileGrpc.SignUpRequest;
import ru.leti.wise.task.profile.ProfileGrpc.SignUpResponse;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.model.Role;
import ru.leti.wise.task.profile.notification.EmailSender;
import ru.leti.wise.task.profile.notification.TemplateLoader;
import ru.leti.wise.task.profile.repository.ProfileRepository;
import ru.leti.wise.task.profile.validation.ProfileValidator;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignUpOperation {

    private final ProfileMapper profileMapper;
    private final ProfileRepository profileRepository;
    private final ProfileValidator profileValidator;
    private final EmailSender emailSender;
    private final TemplateLoader templateLoader;
    @Value("${welcome.template}")
    private String template;

    public SignUpResponse activate(SignUpRequest signUpRequest) {
        var profileDto = signUpRequest.getProfile();
        var isExternal = signUpRequest.getIsExternal();
        log.info("Signing up profile: email='{}', external={}", profileDto.getEmail(), isExternal);
        profileValidator.checkEmptyFields(profileDto, isExternal);
        profileValidator.checkUniqueEmail(profileDto);

        ProfileEntity profile = profileMapper.toProfileEntity(signUpRequest.getProfile());
        profile.setId(UUID.randomUUID());
        profile.setProfileRole(Role.USER);
        profile.setIsExternal(isExternal);
        if (!isExternal) {
            profile.setProfilePassword(BCrypt.hashpw(profile.getProfilePassword(), BCrypt.gensalt()));
        }
        profileRepository.save(profile);
        log.info("Profile created: id={}, email='{}', external={}, role={}",
                profile.getId(), profile.getEmail(), profile.getIsExternal(), profile.getProfileRole());
        String welcomeHtml = templateLoader.loadTemplate(template);
        log.debug("Sending welcome email: id={}, email='{}'", profile.getId(), profile.getEmail());
        emailSender.sendEmail(profile.getEmail(), "Добро пожаловать на WiseTask", welcomeHtml);
        log.info("Profile signed up successfully: id={}, email='{}'", profile.getId(), profile.getEmail());
        return SignUpResponse.newBuilder()
                .setProfile(profileMapper.toProfile(profile))
                .build();
    }
}
