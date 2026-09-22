package ru.leti.wise.task.profile.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileOuterClass.Profile;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.ErrorCode;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.repository.ProfileRepository;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileValidator {

    private final ProfileRepository profileRepository;
    public void checkEmptyFields(Profile profile, Boolean isExternal){
        if(profile.getEmail().isBlank() || (profile.getProfilePassword().isBlank() && !isExternal)
                || profile.getFirstName().isBlank() || profile.getLastName().isBlank()){
            log.warn("Profile fields are not filled: email='{}', external={}", profile.getEmail(), isExternal);
            throw new BusinessException(ErrorCode.EMPTY_FIELDS);
        }
    }
    public void checkEmptyFieldsForUpdate(Profile profile){
        if(profile.getFirstName().isBlank() || profile.getEmail().isBlank() ||profile.getLastName().isBlank()){
            log.warn("Profile fields are not filled for update: id={}, email='{}'", profile.getId(), profile.getEmail());
            throw new BusinessException(ErrorCode.EMPTY_FIELDS);
        }
    }

    public void checkUniqueEmail(Profile profile){
        String email = profile.getEmail();
        profileRepository.findByEmail(email).ifPresent(_ -> {
            log.warn("Profile with email already exists: email='{}'", email);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_TAKEN);
        });
    }

    public void checkEmail(String email){ // здесь можно проверять корректность email
        if(email.isBlank()){
            log.warn("Email is blank");
            throw new BusinessException(ErrorCode.EMPTY_FIELDS);
        }
    }

    public ProfileEntity checkForExistence(String uuid){
        checkUuid(uuid);
        var id = UUID.fromString(uuid);
        return profileRepository.findById(id).orElseThrow(() -> {
            log.warn("Profile not found: id={}", id);
            return new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        });
    }

    public void checkUuid(String uuid){ // здесь можно проверять корректность uuid
        if(uuid.isBlank()){
            log.warn("Profile id is blank");
            throw new BusinessException(ErrorCode.EMPTY_FIELDS);
        }
    }

    public ProfileEntity checkEmailExistence(String email){
        checkEmail(email);
        return profileRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Profile not found by email: email='{}'", email);
            return new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        });
    }

}
