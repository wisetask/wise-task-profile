package ru.leti.wise.task.profile.logic;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.leti.wise.task.profile.ProfileGrpc;
import ru.leti.wise.task.profile.ProfileGrpc.GetAllProfilesResponse;
import ru.leti.wise.task.profile.mapper.ProfileMapper;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.repository.ProfileRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllProfilesOperation {

    private final ProfileMapper profileMapper;
    private final ProfileRepository profileRepository;

    public GetAllProfilesResponse activate(ProfileGrpc.GetAllProfilesRequest request) {
        var filter = request.getFilter();
        var paginationRequest = request.getPagination();
        var pageable = PageRequest.of(paginationRequest.getPage(), paginationRequest.getPageSize());
        var specification = byFilter(
                filter
        );
        var profilePage = profileRepository.findAll(specification, pageable);
        var items = profileMapper.toProfiles(profilePage.getContent());
        var paginationResponse = ProfileGrpc.PaginationResponse.newBuilder()
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
                predicates.add(cb.like(root.get("description"), "%" + filter.getFirstName().toLowerCase() + "%"));
            if (filter.hasLastName())
                predicates.add(cb.like(root.get("category"), "%s" + filter.getLastName().toLowerCase() + "%s"));
            if (filter.hasPatronymic())
                predicates.add(cb.like(root.get("category"), "%s" + filter.getPatronymic().toLowerCase() + "%s"));
            if(!filter.getProfileRoleList().isEmpty()){
                predicates.add(cb.in(root.get("profileRole")).value(filter.getProfileRoleList()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
