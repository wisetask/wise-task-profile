package ru.leti.wise.task.profile.repository;

import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.leti.wise.task.profile.model.ForbiddenWord;

@Observed
@Repository
public interface ForbiddenWordRepository extends JpaRepository<ForbiddenWord, Long> {
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM forbidden_words fw
                WHERE :text ILIKE '%' || fw.word || '%'
            )
            """, nativeQuery = true)
    boolean doesMatchAnyWord(@Param("text") String text);
}
