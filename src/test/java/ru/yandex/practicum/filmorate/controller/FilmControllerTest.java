package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film(
                null,
                "Валидный Фильм",
                "Валидное Описание",
                LocalDate.of(2000, 1, 1),
                120L
        );
    }

    @Test
    void createFilm_withValidData_shouldCreateFilm() {
        Film createdFilm = filmController.createFilm(validFilm);

        assertNotNull(createdFilm.getId());
        assertEquals(1L, createdFilm.getId());
        assertEquals("Валидный Фильм", createdFilm.getName());
    }

    @Test
    void createFilm_withEmptyName_shouldThrowValidationException() {
        Film film = new Film(
                null,
                "",
                "Описание",
                LocalDate.now(),
                120L
        );

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void createFilm_withLongDescription_shouldThrowValidationException() {
        String longDescription = "a".repeat(201);
        Film film = new Film(
                null,
                "Фильм",
                longDescription,
                LocalDate.now(),
                120L
        );

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertTrue(exception.getMessage().contains("Описание не может превышать"));
    }

    @Test
    void createFilm_withEarlyReleaseDate_shouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.of(1895, 12, 27),
                120L
        );

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertTrue(exception.getMessage().contains("Дата релиза не может быть раньше"));
    }

    @Test
    void createFilm_withNegativeDuration_shouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.now(),
                -1L
        );

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    void updateFilm_withNonExistingId_shouldThrowNotFoundException() {
        Film film = new Film(
                999L,
                "Фильм",
                "Описание",
                LocalDate.now(),
                120L
        );

        assertThrows(NotFoundException.class, () -> filmController.updateFilm(film));
    }

    @Test
    void updateFilm_withValidData_shouldUpdateFilm() {
        Film createdFilm = filmController.createFilm(validFilm);
        Film updateData = new Film(
                createdFilm.getId(),
                "Новый Фильм",
                "Новое Описание",
                LocalDate.of(2001, 1, 1),
                90L
        );

        Film updatedFilm = filmController.updateFilm(updateData);

        assertEquals("Новый Фильм", updatedFilm.getName());
        assertEquals("Новое Описание", updatedFilm.getDescription());
        assertEquals(90L, updatedFilm.getDuration());
    }

    @Test
    void getAllFilms_shouldReturnAllFilms() {
        filmController.createFilm(validFilm);
        Film anotherFilm = new Film(
                null,
                "Другой Фильм",
                "Другое Описание",
                LocalDate.now(),
                90L
        );

        filmController.createFilm(anotherFilm);
        assertEquals(2, filmController.getAllFilms().size());
    }
}