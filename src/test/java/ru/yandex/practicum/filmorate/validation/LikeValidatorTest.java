package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.film.GenreRepository;
import ru.yandex.practicum.filmorate.repository.film.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmRepository.class, UserRepository.class, GenreRepository.class, MpaRepository.class,
        FilmService.class, UserService.class, FilmController.class, UserController.class,
        FilmRowMapper.class, UserRowMapper.class, MpaRowMapper.class, GenreRowMapper.class})
class LikeValidatorTest {

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private FilmStorage filmStorage;

    private User validUser;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        validUser = new User(
                1L,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );

        testFilm = new Film(
                1L,
                "Фильм",
                "Описание",
                LocalDate.of(2000, 1, 1),
                120L
        );

        userStorage.addUser(validUser);
        filmStorage.addFilm(testFilm);
    }

    @Test
    @DisplayName("Успешная валидация лайка → не вызывает исключения")
    void validate_WithValidData_ShouldNotThrowException() {
        assertDoesNotThrow(() ->
                LikeValidator.validate(
                        validUser.getId(),
                        testFilm.getId(),
                        userStorage,
                        filmStorage
                )
        );
    }

    @Test
    @DisplayName("Валидация с несуществующим фильмом → исключение NotFoundException")
    void validate_WithNonExistingUser_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> LikeValidator.validate(
                        999L,
                        testFilm.getId(),
                        userStorage,
                        filmStorage
                )
        );

        assertEquals("Фильм с ID 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с несуществующим фильмом → исключение NotFoundException")
    void validate_WithNonExistingFilm_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> LikeValidator.validate(
                        999L,  // несуществующий фильм
                        validUser.getId(),
                        userStorage,
                        filmStorage
                )
        );

        assertEquals("Фильм с ID 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с null userId → исключение ValidationException")
    void validate_WithNullUserId_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> LikeValidator.validate(
                        testFilm.getId(),
                        null,  // null userId
                        userStorage,
                        filmStorage
                )
        );

        assertEquals("ID пользователя не может быть null", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с null filmId → исключение ValidationException")
    void validate_WithNullFilmId_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> LikeValidator.validate(
                        null,  // null filmId
                        validUser.getId(),
                        userStorage,
                        filmStorage
                )
        );

        assertEquals("ID фильма не может быть null", exception.getMessage());
    }
}
