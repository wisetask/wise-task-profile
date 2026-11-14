package ru.leti.wise.task.profile.unit;

import io.grpc.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.leti.wise.task.profile.ProfileGrpc.SignUpRequest;
import ru.leti.wise.task.profile.ProfileOuterClass;
import ru.leti.wise.task.profile.ProfileOuterClass.Profile;
import ru.leti.wise.task.profile.error.BusinessException;
import ru.leti.wise.task.profile.logic.SignUpOperation;
import ru.leti.wise.task.profile.mapper.ProfileMapperImpl;
import ru.leti.wise.task.profile.model.ProfileEntity;
import ru.leti.wise.task.profile.notification.EmailSender;
import ru.leti.wise.task.profile.notification.TemplateLoader;
import ru.leti.wise.task.profile.repository.ForbiddenWordRepository;
import ru.leti.wise.task.profile.repository.ProfileRepository;
import ru.leti.wise.task.profile.validation.ProfileValidator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                SignUpOperation.class,
                ProfileValidator.class,
                ProfileMapperImpl.class
        }
)
public class SignUpOperationTest {
    @Autowired
    private SignUpOperation signUpOperation;

    @MockitoBean
    private ProfileRepository profileRepository;


    @MockitoBean
    private EmailSender emailSender;

    @MockitoBean
    private TemplateLoader templateLoader;

    @MockitoBean
    private ForbiddenWordRepository forbiddenWordRepository;


    @Test
    public void happyTest() {
        Mockito.when(forbiddenWordRepository.doesMatchAnyWord(any())).thenReturn(false);
        Mockito.when(profileRepository.findByEmail(any())).thenReturn(Optional.empty());
        var result = assertDoesNotThrow(() -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile())
                        .setIsExternal(false)
                        .build()
        ));
        Mockito.verify(profileRepository, Mockito.times(1)).save(any());
        var profile = result.getProfile();
        assertEquals("ivanov@mail.com", profile.getEmail());
        assertEquals("", profile.getProfilePassword());
        assertEquals("Иван", profile.getFirstName());
        assertEquals("Иванов", profile.getLastName());
        assertEquals("Иванович", profile.getPatronymic());
        assertEquals(ProfileOuterClass.Role.USER, profile.getProfileRole());
    }

    @Test
    public void creatingProfileThatIsNotUnique() {
        Mockito.when(forbiddenWordRepository.doesMatchAnyWord(any())).thenReturn(false);
        Mockito.when(profileRepository.findByEmail(any())).thenReturn(Optional.of(new ProfileEntity()));
        var exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile())
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals("Пользователь с данной электронной почтой уже существует", exception.getMessage());
        assertEquals(Status.ALREADY_EXISTS, exception.getStatus());
        Mockito.verify(profileRepository, Mockito.times(0)).save(any());
    }

    @Test
    public void creatingProfileWithBadMail() {
        Mockito.when(forbiddenWordRepository.doesMatchAnyWord(any())).thenReturn(false);
        Mockito.when(profileRepository.findByEmail(any())).thenReturn(Optional.empty());
        var exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("ivanov", "123"))
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals(Status.INVALID_ARGUMENT, exception.getStatus());
        assertEquals("Email имеет некорректный вид", exception.getMessage());
        Mockito.verify(profileRepository, Mockito.times(0)).save(any());
    }

    @Test
    public void creatingProfileWithBlankFields() {
        Mockito.when(forbiddenWordRepository.doesMatchAnyWord(any())).thenReturn(false);
        Mockito.when(profileRepository.findByEmail(any())).thenReturn(Optional.empty());
        var exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("", "123"))
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals(Status.INVALID_ARGUMENT, exception.getStatus());
        assertEquals("Не все поля заполнены", exception.getMessage());
        exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("", "Иванов", ""))
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals(Status.INVALID_ARGUMENT, exception.getStatus());
        assertEquals("Не все поля заполнены", exception.getMessage());
        var result = assertDoesNotThrow(() -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("Иван", "Иванов", ""))
                        .setIsExternal(false)
                        .build()
        ));
        var profile = result.getProfile();
        assertEquals("ivanov@mail.com", profile.getEmail());
        assertEquals("", profile.getProfilePassword());
        assertEquals("Иван", profile.getFirstName());
        assertEquals("Иванов", profile.getLastName());
        assertEquals("", profile.getPatronymic());
        Mockito.verify(profileRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void creatingProfileWithForbiddenWordOrNotValidWord() {
        Mockito.when(forbiddenWordRepository.doesMatchAnyWord("BadWord")).thenReturn(true);
        Mockito.when(profileRepository.findByEmail(any())).thenReturn(Optional.empty());
        var exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("BadWord", "123", "VeryBadWord"))
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals(Status.INVALID_ARGUMENT, exception.getStatus());
        assertEquals("Поле 'Имя' использует запрещенное слово", exception.getMessage());
        exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("VeryBadWord", "123", "BadWord"))
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals(Status.INVALID_ARGUMENT, exception.getStatus());
        assertEquals("Поле 'Отчество' использует запрещенное слово", exception.getMessage());
        exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("VeryBadWord", "123##$", "VeryBadWord"))
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals(Status.INVALID_ARGUMENT, exception.getStatus());
        assertEquals("Поле 'Фамилия' неккоректно заполнено", exception.getMessage());
    }

    @Test
    public void creatingProfileWithBadPassword() {
        Mockito.when(forbiddenWordRepository.doesMatchAnyWord("BadWord")).thenReturn(true);
        Mockito.when(profileRepository.findByEmail(any())).thenReturn(Optional.empty());
        var exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("mail@mail.ru", ""))
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals(Status.INVALID_ARGUMENT, exception.getStatus());
        assertEquals("Пароль не может быть пустым или коротким", exception.getMessage());
        assertEquals(Status.INVALID_ARGUMENT, exception.getStatus());
        exception = assertThrows(BusinessException.class, () -> signUpOperation.activate(
                SignUpRequest.newBuilder()
                        .setProfile(createProfile("mail@mail.ru", "12"))
                        .setIsExternal(false)
                        .build()
        ));
        assertEquals("Пароль не может быть пустым или коротким", exception.getMessage());
    }

    private Profile createProfile(
            String email,
            String password
    ) {
        return Profile.newBuilder()
                .setEmail(email)
                .setProfilePassword(password)
                .setFirstName("Иван")
                .setLastName("Иванов")
                .setPatronymic("Иванович")
                .build();
    }

    private Profile createProfile(
            String firstname,
            String lastname,
            String patronymic
    ) {
        return Profile.newBuilder()
                .setEmail("ivanov@mail.com")
                .setProfilePassword("goodPassword123")
                .setFirstName(firstname)
                .setLastName(lastname)
                .setPatronymic(patronymic)
                .build();
    }

    private Profile createProfile() {
        return Profile.newBuilder()
                .setEmail("ivanov@mail.com")
                .setProfilePassword("goodPassword123")
                .setFirstName("Иван")
                .setLastName("Иванов")
                .setPatronymic("Иванович")
                .build();
    }
}
