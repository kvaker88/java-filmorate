package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InternalServerException extends ResponseStatusException {
    public InternalServerException(String reason) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, reason);
    }
}