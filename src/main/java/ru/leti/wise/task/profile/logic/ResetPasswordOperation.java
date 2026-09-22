package ru.leti.wise.task.profile.logic;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileGrpc;
import ru.leti.wise.task.profile.ProfileGrpc.ResetPasswordRequest;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.ErrorCode;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.model.PasswordRecoveryEntity;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.repository.PasswordRecoveryRepository;
import ru.leti.wise.task.profile.repository.ProfileRepository;
import ru.leti.wise.task.profile.validation.ProfileValidator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetPasswordOperation {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final PasswordRecoveryRepository passwordRecoveryRepository;
    private final ProfileValidator profileValidator;


    @Transactional
    public ProfileGrpc.ResetPasswordResponse activate(ResetPasswordRequest resetPasswordRequest) {
        log.info("Resetting password by recovery token");
        PasswordRecoveryEntity passwordRecoveryEntity = passwordRecoveryRepository
                .findFirstByRecoveryTokenOrderByExpiresAtDesc(UUID.fromString(resetPasswordRequest.getRecoveryToken()))
                .orElseThrow(() -> {
                    log.warn("Recovery token not found");
                    return new BusinessException(ErrorCode.UNKNOWN_LINK);
                });
        if(passwordRecoveryEntity.getExpiresAt().before(Timestamp.valueOf(LocalDateTime.now()))){
            log.warn("Recovery token expired: email='{}', expiresAt={}",
                    passwordRecoveryEntity.getEmail(), passwordRecoveryEntity.getExpiresAt());
            throw new BusinessException(ErrorCode.UNKNOWN_LINK);
        }
        ProfileEntity profile = profileValidator.checkEmailExistence(passwordRecoveryEntity.getEmail());

        profile.setProfilePassword(BCrypt.hashpw(resetPasswordRequest.getNewPassword(), BCrypt.gensalt()));
        profileRepository.save(profile);
        passwordRecoveryRepository.deleteAllByEmail(passwordRecoveryEntity.getEmail());
        log.info("Password reset: profileId={}, email='{}'", profile.getId(), profile.getEmail());
        return ProfileGrpc.ResetPasswordResponse.newBuilder()
                .setProfile(profileMapper.toProfile(profile))
                .build();
    }
}

