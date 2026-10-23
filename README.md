# wise-task-profile

Микросервис профилей пользователей. Хранит аккаунты, роли и хеши паролей; отвечает за регистрацию, вход, смену и сброс пароля, а также статистику по пользователю.

## Технологии

- Java 25, Spring Boot 4.1.x, gRPC-сервер
- Spring Data JPA + PostgreSQL (база `wise_task_profile`, схема ведётся Liquibase — `db/database-changelog.xml`)
- BCrypt для хеширования паролей
- SMTP (boky/postfix) для писем сброса пароля

## Домен

```
Profile: id (UUID), email, firstName, lastName, patronymic,
         profileRole (USER | AUTHOR | ADMIN), profilePassword (BCrypt),
         studentGroup, studentCourse
password_recovery: recoveryToken, email, createdAt, expiresAt
```

Выдачей JWT занимается gateway (ключи RSA у него); этот сервис проверяет креды и отдаёт профиль по email при валидации токена.

## gRPC API (порт 6567)

Proto: `src/main/resources/proto/profile_grpc.proto`, пакет `ru.leti.wise.task.profile`.

| Метод | Назначение |
|---|---|
| `SignUp` / `SignIn` | Регистрация / проверка email+пароля (BCrypt) |
| `SendResetPasswordEmail` | Отправить письмо со ссылкой сброса (SMTP) |
| `ResetPassword` | Сброс пароля по токену из письма |
| `ChangePassword` | Смена пароля со старым паролем |
| `GetProfile` / `GetProfileByEmail` / `GetProfilesByIds` | Получение профилей |
| `GetAllProfiles` | Список с пагинацией и фильтрами |
| `UpdateProfile` / `DeleteProfile` | Управление профилем |
| `GetProfileStatistic` | Статистика пользователя (по данным event-сервиса) |

Фильтры `GetAllProfiles`: `email`, `firstName`, `lastName`, `patronymic` (like, без регистра), `profileRole` (список ролей).

## Конфигурация

| Переменная | Назначение |
|---|---|
| `PROFILE_GRPC_PORT` | Порт gRPC-сервера (6567) |
| `PROFILE_PORT` | Внутренний HTTP-порт (8082, actuator) |
| `PROFILE_DB`, `PROFILE_DB_USER`, `PROFILE_DB_PASSWORD` | Подключение к PostgreSQL |
| `SMTP_HOST`, `SMTP_PORT` | Почтовый сервер для писем сброса пароля |

## Запуск

Через общий docker-стенд gateway:

```bash
cd ../wise-task-gateway && docker compose up -d --build wise-task-profile
```

Локально: `./gradlew bootRun` (нужны PostgreSQL с базой `wise_task_profile` и SMTP).
