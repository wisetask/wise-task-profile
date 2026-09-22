package ru.leti.wise.task.profile.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileGrpc.ProfileIds;
import ru.leti.wise.task.profile.ProfileGrpc.Profiles;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.repository.ProfileRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetProfilesByIdsOperation {

    private final ProfileMapper profileMapper;
    private final ProfileRepository profileRepository;

    public Profiles activate(ProfileIds request) {
        var ids = request.getProfileIdsList()
                .stream()
                .filter(id -> !id.isBlank())
                .map(UUID::fromString)
                .toList();
        log.debug("Fetching profiles by ids: requested={}, uniqueIds={}",
                request.getProfileIdsList().size(), ids.size());
        var profiles = ids.isEmpty() ? List.<ProfileEntity>of() : profileRepository.findAllById(ids);
        log.debug("Profiles fetched by ids: requested={}, found={}", ids.size(), profiles.size());
        return Profiles.newBuilder()
                .addAllProfiles(profileMapper.toProfiles(profiles))
                .build();
    }
}
