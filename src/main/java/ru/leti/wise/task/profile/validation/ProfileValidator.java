package ru.leti.wise.task.profile.validation;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileOuterClass.Profile;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.ErrorCode;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.repository.ForbiddenWordRepository;
import ru.leti.wise.task.profile.repository.ProfileRepository;

import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileValidator {
    private static final Pattern VALID_NICK = Pattern.compile("^(?=.*[a-zA-Zа-яА-Я0-9])[a-zA-Zа-яА-Я0-9._-]{2,20}$");
    private static final Pattern VALID_EMAIL = Pattern
            .compile("^[A-Za-z0-9_%+-]+(?:\\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$");
    private final ForbiddenWordRepository forbiddenWordRepository;
    private final ProfileRepository profileRepository;

    public void checkEmptyFields(Profile profile) {
        if (profile.getEmail().isBlank() || profile.getFirstName().isBlank() || profile.getLastName().isBlank()) {
            log.warn("Profile fields are not filled, email='{}'", profile.getEmail());
            throw new BusinessException(ErrorCode.EMPTY_FIELDS);
        }
    }

    public void checkPassword(Profile profile, Boolean isExternal) {
        if ((profile.getProfilePassword().isBlank() && !isExternal)) {
            log.warn("Profile password is invalid, email: {}", profile.getEmail());
            throw new BusinessException(ErrorCode.EMPTY_FIELDS);
        }
    }

    public void checkUniqueEmail(Profile profile) {
        String email = profile.getEmail();
        profileRepository.findByEmail(email).ifPresent(_ -> {
            log.warn("Profile with email already exists: email='{}'", email);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_TAKEN);
        });
    }

    public void checkFieldValue(String field, String fieldValue) {
        if (!VALID_NICK.matcher(fieldValue).find()) {
            log.warn("Field '{}' is not valid: '{}'", field, fieldValue);
            throw new BusinessException("Поле '%s' неккоректно заполнено".formatted(field), Status.INVALID_ARGUMENT);
        }
        if (forbiddenWordRepository.doesMatchAnyWord(fieldValue)) {
            log.warn("Field '{}' uses forbidden word: '{}'", field, fieldValue);
            throw new BusinessException("Поле '%s' использует запрещенное слово".formatted(field), Status.INVALID_ARGUMENT);
        }
    }

    public void checkEmail(String email) {
        if (!isValidEmail(email)) {
            log.warn("Email validation exception for email: {}", email);
            throw new BusinessException("Email имеет некорректный вид", Status.INVALID_ARGUMENT);
        }
    }

    public boolean isValidEmail(String email) {
        if (email.isBlank() || email.length() > 254) return false;
        int at = email.indexOf('@');
        if (at < 1 || at > 64) return false;
        return VALID_EMAIL.matcher(email).matches();
    }

    public void checkProfile(Profile profile) {
        checkEmptyFields(profile);
        checkFieldValue("Имя", profile.getFirstName());
        checkFieldValue("Фамилия", profile.getLastName());
        if (!profile.getPatronymic().isBlank())
            checkFieldValue("Отчество", profile.getPatronymic());
        checkEmail(profile.getEmail());
    }

    public ProfileEntity checkForExistence(String uuid) {
        checkUuid(uuid);
        var id = UUID.fromString(uuid);
        return profileRepository.findById(id).orElseThrow(() -> {
            log.warn("Profile not found: id={}", id);
            return new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        });
    }

    public void checkUuid(String uuid) { // здесь можно проверять корректность uuid
        if (uuid.isBlank()) {
            log.warn("Profile id is blank");
            throw new BusinessException(ErrorCode.EMPTY_FIELDS);
        }
    }

    public ProfileEntity checkEmailExistence(String email) {
        checkEmail(email);
        return profileRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Profile not found by email: email='{}'", email);
            return new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        });
    }

}
