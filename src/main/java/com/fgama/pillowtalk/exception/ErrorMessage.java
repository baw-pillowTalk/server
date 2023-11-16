package com.fgama.pillowtalk.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ErrorMessage {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<ValidationError> errors;

    private final int code;
    private final String errorSimpleName;
    private final String message;
    private final LocalDateTime timeStamp;

    public static ErrorMessage of(@NotNull Exception exception,
                                  @NotNull HttpStatus httpStatus
    ) {
        return ErrorMessage.builder()
                .code(httpStatus.value())
                .errorSimpleName(exception.getClass().getSimpleName())
                .message(exception.getLocalizedMessage())
                .timeStamp(LocalDateTime.now())
                .build();
    }

    public static ErrorMessage of(Exception exception,
                                  HttpStatus httpStatus,
                                  List<ValidationError> errors
    ) {
        return ErrorMessage.builder()
                .code(httpStatus.value())
                .errorSimpleName(exception.getClass().getSimpleName())
                .message(exception.getLocalizedMessage())
                .timeStamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

    @Getter
    @Builder
    @RequiredArgsConstructor
    public static class ValidationError {
        private final String field;
        private final String message;

        public static ValidationError of(final FieldError fieldError) {
            return ValidationError.builder()
                    .field(fieldError.getField())
                    .message(fieldError.getDefaultMessage())
                    .build();
        }
    }
}
