package ru.leti.wise.task.profile.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import ru.leti.wise.task.profile.model.ForbiddenWord;
import ru.leti.wise.task.profile.repository.ForbiddenWordRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class ForbiddenWordIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private ForbiddenWordRepository forbiddenWordRepository;

    @Test
    @Transactional
    public void testSimpleWordCheck() {
        forbiddenWordRepository.save(ForbiddenWord.builder()
                        .word("арбуз")
                .build());
        assertTrue(forbiddenWordRepository.doesMatchAnyWord("тестарбузтест"));
        assertTrue(forbiddenWordRepository.doesMatchAnyWord("АРбузтест"));
        assertTrue(forbiddenWordRepository.doesMatchAnyWord("ТЕСТАРбуЗ"));
    }


    @Test
    @Transactional
    public void testWordThatNotInDb() {
        forbiddenWordRepository.save(ForbiddenWord.builder()
                .word("арбуз")
                .build());
        assertFalse(forbiddenWordRepository.doesMatchAnyWord("хлебтестхлеб"));
    }

}
