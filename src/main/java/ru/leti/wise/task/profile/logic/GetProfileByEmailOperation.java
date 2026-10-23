package ru.leti.wise.task.profile.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileGrpc;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.error.ErrorCode;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.repository.ProfileRepository;
import ru.leti.wise.task.profile.validation.ProfileValidator;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetProfileByEmailOperation {

    private final ProfileMapper profileMapper;
    private final ProfileValidator profileValidator;

    public ProfileGrpc.GetProfileByEmailResponse activate(String email) {
        log.debug("Fetching profile by email: email='{}'", email);
        var profile = profileValidator.checkEmailExistence(email);
        log.debug("Profile found by email: id={}, email='{}'", profile.getId(), profile.getEmail());
        return ProfileGrpc.GetProfileByEmailResponse.newBuilder()
                .setProfile(profileMapper.toProfile(profile))
                .build();
    }
}
