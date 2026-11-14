package ru.leti.wise.task.profile.error;

import io.grpc.Status;
import lombok.Getter;

@Getter
public enum ErrorCode {
    PROFILE_NOT_FOUND("Профиль не найден", Status.NOT_FOUND),
    INVALID_PASSWORD("Неправильный пароль", Status.UNAUTHENTICATED),
    EMAIL_ALREADY_TAKEN("Пользователь с данной электронной почтой уже существует", Status.ALREADY_EXISTS),
    UNKNOWN_LINK("Некорректный токен", Status.INVALID_ARGUMENT),
    EMPTY_FIELDS("Не все поля заполнены", Status.INVALID_ARGUMENT),
    MANY_REQUESTS("Слишком много запросов", Status.RESOURCE_EXHAUSTED);

    final String message;
    final Status status;

    ErrorCode(String message, Status status) {
        this.message = message;
        this.status = status;
    }
}
