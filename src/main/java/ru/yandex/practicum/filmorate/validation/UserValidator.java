package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public final class UserValidator {

    private UserValidator() {
    }

    public static void validate(User user) {
        log.info("Начинается валидация всех полей пользователя");

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Email пользователя пуст или не содержит символ @ = {}", user.getEmail());
            throw new ValidationException("Email должен содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Логин пользователя пуст");
            throw new ValidationException("Логин не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            log.warn("Логин пользователя содержит пробелы = {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getBirthday() == null) {
            log.warn("Дата рождения пользователя не указана");
            throw new ValidationException("Дата рождения должна быть указана");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения пользователя указана в будущем = {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        log.info("Полная валидация пользователя завершена");
    }

    public static void validateName(User user) {
        log.info("Начинается валидация имени пользователя");

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пустое, используется логин");
            user.setName(user.getLogin());
        }

        log.info("Валидация имени пользователя завершена");
    }
}