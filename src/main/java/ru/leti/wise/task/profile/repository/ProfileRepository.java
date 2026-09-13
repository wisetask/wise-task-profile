package ru.leti.wise.task.profile.repository;

import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import ru.leti.wise.task.profile.model.ProfileEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Observed
public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID>, JpaSpecificationExecutor<ProfileEntity> {
    Optional<ProfileEntity> findByEmail(String email);
}
