package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
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
class FilmValidatorTest {

    @Test
    @DisplayName("Валидация корректного фильма → не вызывает исключения")
    void validate_WithtestFilm_ShouldNotThrowException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.of(2000, 1, 1),
                120L
        );
        assertDoesNotThrow(() -> FilmValidator.validate(film));
    }

    @Test
    @DisplayName("Валидация фильма с пустым названием → исключение ValidationException")
    void validate_WithBlankName_ShouldThrowValidationException() {
        Film film = new Film(
                null,
                "",
                "Описание",
                LocalDate.of(2000, 1, 1),
                120L
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> FilmValidator.validate(film)
        );
        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация фильма с слишком длинным описанием → исключение ValidationException")
    void validate_WithLongDescription_ShouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Очень длинное описание ".repeat(20), // > 200 символов
                LocalDate.of(2000, 1, 1),
                120L
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> FilmValidator.validate(film)
        );
        assertTrue(exception.getMessage().contains("Описание не может превышать 200 символов"));
    }

    @Test
    @DisplayName("Валидация фильма с null датой релиза → исключение ValidationException")
    void validate_WithNullReleaseDate_ShouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                null,
                120L
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> FilmValidator.validate(film)
        );
        assertEquals("Дата релиза должна быть указана", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация фильма с датой релиза до 28.12.1895 → исключение ValidationException")
    void validate_WithEarlyReleaseDate_ShouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.of(1895, 12, 27),
                120L
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> FilmValidator.validate(film)
        );
        assertTrue(exception.getMessage().contains("Дата релиза не может быть раньше 1895-12-28"));
    }

    @Test
    @DisplayName("Валидация фильма с нулевой продолжительностью → исключение ValidationException")
    void validate_WithZeroDuration_ShouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.of(2000, 1, 1),
                0L
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> FilmValidator.validate(film)
        );
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация фильма с отрицательной продолжительностью → исключение ValidationException")
    void validate_WithNegativeDuration_ShouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.of(2000, 1, 1),
                -120L
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> FilmValidator.validate(film)
        );
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация фильма с null продолжительностью → исключение ValidationException")
    void validate_WithNullDuration_ShouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.of(2000, 1, 1),
                null
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> FilmValidator.validate(film)
        );
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }
}
