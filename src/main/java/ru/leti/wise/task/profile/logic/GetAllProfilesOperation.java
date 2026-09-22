package ru.leti.wise.task.profile.logic;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.pagination.Pagination;
import ru.leti.wise.task.profile.ProfileGrpc;
import ru.leti.wise.task.profile.ProfileGrpc.GetAllProfilesResponse;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.model.Role;
import ru.leti.wise.task.profile.repository.ProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAllProfilesOperation {

    private final ProfileMapper profileMapper;
    private final ProfileRepository profileRepository;

    public GetAllProfilesResponse activate(ProfileGrpc.GetAllProfilesRequest request) {
        var filter = request.getFilter();
        var paginationRequest = request.getPagination();
        log.debug("Fetching profiles: page={}, pageSize={}",
                paginationRequest.getPage(), paginationRequest.getPageSize());
        var pageable = PageRequest.of(paginationRequest.getPage(), paginationRequest.getPageSize());
        var specification = byFilter(
                filter
        );
        var profilePage = profileRepository.findAll(specification, pageable);
        var items = profileMapper.toProfiles(profilePage.getContent());
        log.debug("Profiles fetched: page={}, pageSize={}, returned={}, total={}",
                profilePage.getNumber(), profilePage.getSize(), profilePage.getNumberOfElements(),
                profilePage.getTotalElements());
        var paginationResponse = Pagination.PaginationResponse.newBuilder()
                .setPage(profilePage.getNumber())
                .setPageSize(profilePage.getSize())
                .setTotalCount(profilePage.getTotalElements())
                .setTotalPages(profilePage.getTotalPages())
                .setHasNext(profilePage.hasNext())
                .setHasPrevious(profilePage.hasPrevious())
                .build();
        return GetAllProfilesResponse.newBuilder()
                .addAllItems(items)
                .setPagination(paginationResponse)
                .build();
    }

    public Specification<ProfileEntity> byFilter(ProfileGrpc.ProfileFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.hasEmail())
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + filter.getEmail().toLowerCase() + "%"));
            if (filter.hasFirstName())
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + filter.getFirstName().toLowerCase() + "%"));
            if (filter.hasLastName())
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + filter.getLastName().toLowerCase() + "%"));
            if (filter.hasPatronymic())
                predicates.add(cb.like(cb.lower(root.get("patronymic")), "%" + filter.getPatronymic().toLowerCase() + "%"));
            var roles = toRoles(filter.getProfileRoleList());
            if (!roles.isEmpty())
                predicates.add(root.get("profileRole").in(roles));

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private List<Role> toRoles(List<String> profileRoles) {
        return profileRoles.stream()
                .map(role -> {
                    try {
                        return Role.valueOf(role.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown role in profile filter, skipped: {}", role);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
