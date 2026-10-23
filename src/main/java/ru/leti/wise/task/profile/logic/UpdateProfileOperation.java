package ru.leti.wise.task.profile.logic;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileGrpc.UpdateProfileResponse;
import ru.leti.wise.task.profile.ProfileOuterClass.Profile;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.ErrorCode;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.repository.ProfileRepository;
import ru.leti.wise.task.profile.validation.ProfileValidator;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateProfileOperation {

    private final ProfileMapper profileMapper;
    private final ProfileRepository profileRepository;
    private final ProfileValidator profileValidator;

    public UpdateProfileResponse activate(Profile profile) {
        log.info("Updating profile: id={}, email='{}'", profile.getId(), profile.getEmail());
        profileValidator.checkEmptyFieldsForUpdate(profile);
        var oldProfile = profileValidator.checkForExistence(profile.getId());
        profileMapper.updateProfileEntity(oldProfile, profile);
        profileRepository.save(oldProfile);
        log.info("Profile updated: id={}, email='{}'", oldProfile.getId(), oldProfile.getEmail());

        return UpdateProfileResponse.newBuilder()
                .setProfile(profileMapper.toProfile(oldProfile))
                .build();
    }
}
