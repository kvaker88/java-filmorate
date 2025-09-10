package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.film.GenreRepository;
import ru.yandex.practicum.filmorate.repository.film.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmRepository.class, UserRepository.class, GenreRepository.class, MpaRepository.class, FilmService.class, UserService.class, FilmController.class, UserController.class, FilmRowMapper.class, UserRowMapper.class, MpaRowMapper.class, GenreRowMapper.class})
class UserValidatorTest {
    private final User validUser = new User(
            1L,
            "mail@yandex.ru",
            "Логин",
            "Имя",
            LocalDate.of(1990, 1, 1)
    );

    @Test
    @DisplayName("Успешная валидация пользователя → не вызывает исключения")
    void validate_WithValidUser_ShouldNotThrowException() {
        assertDoesNotThrow(() -> UserValidator.validate(validUser));
    }

    @Test
    @DisplayName("Валидация с некорректным email (без @) → исключение ValidationException")
    void validate_WithInvalidEmail_ShouldThrowValidationException() {
        User user = new User(
                null,
                "Некорректный-Емейл",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validate(user)
        );

        assertEquals("Email должен содержать символ @", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с пустым логином → исключение ValidationException")
    void validate_WithBlankLogin_ShouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validate(user)
        );

        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с логином содержащим пробелы → исключение ValidationException")
    void validate_WithLoginContainingSpaces_ShouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин с пробелами",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validate(user)
        );

        assertEquals("Логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с null датой рождения → исключение ValidationException")
    void validate_WithNullBirthday_ShouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                null
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validate(user)
        );

        assertEquals("Дата рождения должна быть указана", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с датой рождения в будущем → исключение ValidationException")
    void validate_WithFutureBirthday_ShouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.now().plusDays(1)
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validate(user)
        );

        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация имени (подстановка логина при пустом имени) → имя пользователя соответствует логину")
    void validateName_WithBlankName_ShouldSetLoginAsName() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "",
                LocalDate.of(1990, 1, 1)
        );

        UserValidator.validateName(user);

        assertEquals("Логин", user.getName());
    }

    @Test
    @DisplayName("Валидация имени (сохранение имени если оно не пустое) → " +
            "имя пользователя соответствует переданному имени")
    void validateName_WithNonBlankName_ShouldKeepName() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );

        UserValidator.validateName(user);

        assertEquals("Имя", user.getName());
    }
}
