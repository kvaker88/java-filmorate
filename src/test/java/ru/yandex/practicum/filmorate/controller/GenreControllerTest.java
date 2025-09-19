package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.film.GenreRepository;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({GenreRepository.class, GenreService.class, GenreController.class, GenreRowMapper.class,
        FilmRepository.class, FilmRowMapper.class, MpaRowMapper.class})
class GenreControllerTest {

    @Autowired
    private GenreController genreController;

    @Test
    @DisplayName("Получение всех жанров → возвращает непустой список")
    void getAllGenres_shouldReturnNonEmptyCollection() {
        Collection<Genre> genres = genreController.getAllGenres();

        assertNotNull(genres);
        assertFalse(genres.isEmpty());
    }

    @Test
    @DisplayName("Получение жанра по существующему ID → возвращает корректный жанр")
    void getGenreById_withExistingId_shouldReturnGenre() {
        Genre genre = genreController.getGenreById(1L);

        assertNotNull(genre);
        assertEquals(1L, genre.getId());
        assertNotNull(genre.getName());
    }

    @Test
    @DisplayName("Получение жанра по несуществующему ID → исключение NotFoundException")
    void getGenreById_withNonExistingId_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> genreController.getGenreById(9999L));
    }
}