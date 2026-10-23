package ru.leti.wise.task.profile.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileGrpc.GetProfileRequest;
import ru.leti.wise.task.profile.ProfileGrpc.GetProfileResponse;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.ErrorCode;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.repository.ProfileRepository;
import ru.leti.wise.task.profile.validation.ProfileValidator;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetProfileOperation {

    private final ProfileMapper profileMapper;
    private final ProfileValidator profileValidator;

    public GetProfileResponse activate(GetProfileRequest request) {
        log.debug("Fetching profile: id={}", request.getProfileId());
        var profile = profileValidator.checkForExistence(request.getProfileId());
        log.debug("Profile found: id={}, email='{}', role={}",
                profile.getId(), profile.getEmail(), profile.getProfileRole());

        return GetProfileResponse.newBuilder()
                .setProfile(profileMapper.toProfile(profile))
                .build();
    }
}
