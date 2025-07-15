package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LikeValidatorTest {
    private UserStorage userStorage;
    private FilmStorage filmStorage;
    private User validUser;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();

        validUser = new User(
                1L,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );

        validFilm = new Film(
                1L,
                "Фильм",
                "Описание",
                LocalDate.of(2000, 1, 1),
                120L
        );

        userStorage.addUser(validUser);
        filmStorage.addFilm(validFilm);
    }

    @Test
    @DisplayName("Успешная валидация лайка → не вызывает исключения")
    void validate_WithValidData_ShouldNotThrowException() {
        assertDoesNotThrow(() ->
                LikeValidator.validate(
                        validUser.getId(),
                        validFilm.getId(),
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
                        validFilm.getId(),
                        userStorage,
                        filmStorage
                )
        );

        assertEquals("Фильм с ID 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с несуществующим пользователем → исключение NotFoundException")
    void validate_WithNonExistingFilm_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> LikeValidator.validate(
                        validUser.getId(),
                        999L,
                        userStorage,
                        filmStorage
                )
        );

        assertEquals("Пользователь с ID 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с null userId → исключение NullPointerException")
    void validate_WithNullUserId_ShouldThrowNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> LikeValidator.validate(
                        null,
                        validFilm.getId(),
                        userStorage,
                        filmStorage
                )
        );
    }

    @Test
    @DisplayName("Валидация с null filmId → исключение NullPointerException")
    void validate_WithNullFilmId_ShouldThrowNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> LikeValidator.validate(
                        validUser.getId(),
                        null,
                        userStorage,
                        filmStorage
                )
        );
    }
}
