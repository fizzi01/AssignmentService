package it.unisalento.pasproject.assignmentservice.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public abstract class CustomErrorException extends RuntimeException {
    private CustomErrorResponse errorResponse;

    public CustomErrorException(String message, HttpStatus status) {
        super(message);
        this.errorResponse = CustomErrorResponse.builder()
                .status(status)
                .message(message)
                .timestamp(OffsetDateTime.now().toString())
                .traceId(UUID.randomUUID().toString())
                .build();
    }
}
