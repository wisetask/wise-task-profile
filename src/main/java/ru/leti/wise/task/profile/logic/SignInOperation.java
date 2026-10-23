package ru.leti.wise.task.profile.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileGrpc.SignInRequest;
import ru.leti.wise.task.profile.ProfileGrpc.SignInResponse;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.ErrorCode;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.validation.ProfileValidator;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignInOperation {

    private final ProfileMapper profileMapper;
    private final ProfileValidator profileValidator;
    public SignInResponse activate(SignInRequest request) {

        log.info("Signing in profile: email='{}'", request.getEmail());
        ProfileEntity profile = profileValidator.checkEmailExistence(request.getEmail());

        if (request.getPassword().isBlank() || !BCrypt.checkpw(request.getPassword(),profile.getProfilePassword())) {
            log.warn("Invalid password for profile: id={}, email='{}'", profile.getId(), request.getEmail());
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        log.info("Profile signed in: id={}, email='{}'", profile.getId(), profile.getEmail());
        return SignInResponse.newBuilder()
                .setProfile(profileMapper.toProfile(profile))
                .build();
    }
}
