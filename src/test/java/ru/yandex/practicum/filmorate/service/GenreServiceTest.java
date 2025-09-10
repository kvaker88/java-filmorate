package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.film.GenreRepository;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({GenreRepository.class, GenreService.class, GenreController.class, GenreRowMapper.class,
        FilmRepository.class, FilmRowMapper.class, MpaRowMapper.class})
class GenreServiceTest {

    @Autowired
    private GenreService genreService;

    @Test
    @DisplayName("Получение всех жанров → возвращает непустой список")
    void getAllGenres_shouldReturnNonEmptyList() {
        List<Genre> genres = genreService.getAllGenres();

        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        assertTrue(genres.size() >= 1);
    }

    @Test
    @DisplayName("Получение жанра по существующему ID → возвращает корректный жанр")
    void getGenreById_withExistingId_shouldReturnGenre() {
        Genre foundGenre = genreService.getGenreById(1L);

        assertNotNull(foundGenre);
    }

    @Test
    @DisplayName("Получение жанра по несуществующему ID → исключение NotFoundException")
    void getGenreById_withNonExistingId_shouldThrowNotFoundException() {
        Long nonExistingId = 9999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> genreService.getGenreById(nonExistingId)
        );

        assertEquals("Жанр с ID " + nonExistingId + " не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Получение жанра по ID = null → исключение NotFoundException")
    void getGenreById_withNullId_shouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> genreService.getGenreById(null)
        );

        assertTrue(exception.getMessage().contains("не найден"));
    }

    @Test
    @DisplayName("Получение жанра по отрицательному ID → исключение NotFoundException")
    void getGenreById_withNegativeId_shouldThrowNotFoundException() {
        Long negativeId = -1L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> genreService.getGenreById(negativeId)
        );

        assertEquals("Жанр с ID " + negativeId + " не найден", exception.getMessage());
    }
}