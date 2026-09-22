package ru.leti.wise.task.profile.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCrypt;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.ErrorCode;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.repository.ProfileRepository;
import ru.leti.wise.task.profile.ProfileGrpc;
import ru.leti.wise.task.profile.ProfileGrpc.ChangePasswordRequest;
import ru.leti.wise.task.profile.validation.ProfileValidator;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangePasswordOperation {
    private final ProfileRepository profileRepository;
    private final ProfileValidator profileValidator;

    public void activate(ChangePasswordRequest request) {
        log.info("Changing password: profileId={}", request.getProfileId());
        ProfileEntity profile = profileValidator.checkForExistence(request.getProfileId());
        if (!BCrypt.checkpw(request.getOldPassword(), profile.getProfilePassword())) {
            log.warn("Invalid old password for profile: id={}", profile.getId());
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        profile.setProfilePassword(BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt()));
        profileRepository.save(profile);
        log.info("Password changed: profileId={}", profile.getId());
    }
}
