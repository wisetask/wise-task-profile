package ru.leti.wise.task.profile.mapper;

import org.mapstruct.*;
import ru.leti.wise.task.profile.ProfileOuterClass;
import ru.leti.wise.task.profile.ProfileOuterClass.Profile;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.model.Role;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProfileMapper {

    @Mapping(target = "profilePassword", ignore = true)
    Profile toProfile(ProfileEntity profile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isExternal", constant = "false")
    ProfileEntity toProfileEntity(Profile profile);

    @Mapping(target = "id",
            conditionExpression = "java(profile.getId() != null && !profile.getId().isEmpty())",
            defaultExpression = "java(mapStringToUuid(profile.getId()))")
    List<Profile> toProfiles(List<ProfileEntity> profiles);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profilePassword", ignore = true)
    void updateProfileEntity(@MappingTarget ProfileEntity target, Profile source);

    default ProfileOuterClass.Role toRole(Role role) {
        return ProfileOuterClass.Role.valueOf(role.name());
    }

    default Role toRole(ProfileOuterClass.Role role) {
        return Role.valueOf(role.name());
    }

    default UUID mapStringToUuid(String value) {
        return value != null ? UUID.fromString(value) : null;
    }

    default String mapUuidToString(UUID value) {
        return value != null ? value.toString() : "";
    }
}
