package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.dto.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorResponse handleNotFound(final NotFoundException e) {
        return new ErrorResponse(
                "Не удалось найти объект",
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleValidation(final  ValidationException e) {
        return new ErrorResponse(
                "Ошибка валидации данных",
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleInvalidFriendship(final InvalidFriendshipException e) {
        return new ErrorResponse(
                "Ошибка валидации дружбы",
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleInternalError(final Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return new ErrorResponse(
                "Внутренняя ошибка сервера",
                e.getMessage()
        );
    }
}