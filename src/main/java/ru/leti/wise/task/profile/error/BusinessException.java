package ru.leti.wise.task.profile.error;

import io.grpc.Status;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BusinessException extends RuntimeException {
    private String message;
    private Status status;

    public BusinessException(String message, Status status) {
        super(message);
        this.message = message;
        this.status = status;
    }

    public BusinessException(ErrorCode code) {
        super(code.message);
        this.message = code.message;
        this.status = code.getStatus();
    }
}
