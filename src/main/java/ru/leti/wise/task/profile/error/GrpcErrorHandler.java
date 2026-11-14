package ru.leti.wise.task.profile.error;

import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrpcErrorHandler {
    public Status processError(BusinessException e) {
        return e.getStatus().withDescription(e.getMessage());
    }
}
