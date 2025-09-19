package ru.yandex.practicum.filmorate.repository.film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({GenreRepository.class, GenreService.class, GenreController.class, GenreRowMapper.class,
        FilmRepository.class, FilmRowMapper.class, MpaRowMapper.class})
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film(
                null,
                "Валидный Фильм",
                "Валидное Описание",
                LocalDate.of(2000, 1, 1),
                120L
        );
    }

    @Test
    @DisplayName("Получение всех жанров → возвращает непустой список")
    void findAll_shouldReturnNonEmptyList() {
        List<Genre> genres = genreRepository.findAll();

        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        assertTrue(genres.size() >= 1);
    }

    @Test
    @DisplayName("Получение жанра по существующему ID → возвращает Optional с жанром")
    void findById_withExistingId_shouldReturnGenre() {
        Optional<Genre> foundGenre = genreRepository.findById(1L);

        assertTrue(foundGenre.isPresent());
        assertEquals(1L, foundGenre.get().getId());
        assertEquals("Комедия", foundGenre.get().getName());
    }

    @Test
    @DisplayName("Получение жанра по несуществующему ID → возвращает пустой Optional")
    void findById_withNonExistingId_shouldReturnEmptyOptional() {
        Optional<Genre> foundGenre = genreRepository.findById(9999L);

        assertFalse(foundGenre.isPresent());
    }

    @Test
    @DisplayName("Получение жанров по ID фильма → возвращает пустой список для фильма без жанров")
    void findGenresByFilmId_withFilmWithoutGenres_shouldReturnEmptyList() {
        List<Genre> genres = genreRepository.findGenresByFilmId(testFilm.getId());

        assertNotNull(genres);
        assertTrue(genres.isEmpty());
    }

    @Test
    @DisplayName("Добавление пустого набора жанров → не вызывает ошибок")
    void addGenresToFilm_withEmptySet_shouldNotThrowError() {
        Set<Genre> emptyGenres = Set.of();

        assertDoesNotThrow(() -> genreRepository.addGenresToFilm(testFilm.getId(), emptyGenres));
    }

    @Test
    @DisplayName("Добавление null жанров → не вызывает ошибок")
    void addGenresToFilm_withNullSet_shouldNotThrowError() {
        assertDoesNotThrow(() -> genreRepository.addGenresToFilm(testFilm.getId(), null));
    }

    @Test
    @DisplayName("Удаление жанров из фильма без жанров → не вызывает ошибок")
    void removeAllGenresFromFilm_withFilmWithoutGenres_shouldNotThrowError() {
        assertDoesNotThrow(() -> genreRepository.removeAllGenresFromFilm(testFilm.getId()));

        List<Genre> genres = genreRepository.findGenresByFilmId(testFilm.getId());
        assertTrue(genres.isEmpty());
    }

    @Test
    @DisplayName("Проверка существования жанра по существующему ID → возвращает true")
    void existsById_withExistingId_shouldReturnTrue() {
        boolean exists = genreRepository.existsById(1L);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Проверка существования жанра по несуществующему ID → возвращает false")
    void existsById_withNonExistingId_shouldReturnFalse() {
        boolean exists = genreRepository.existsById(9999L);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Проверка существования жанра по null ID → возвращает false")
    void existsById_withNullId_shouldReturnFalse() {
        boolean exists = genreRepository.existsById(null);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Получение жанров отсортированными по ID")
    void findAll_shouldReturnGenresSortedById() {
        List<Genre> genres = genreRepository.findAll();

        for (int i = 0; i < genres.size() - 1; i++) {
            assertTrue(genres.get(i).getId() <= genres.get(i + 1).getId());
        }
    }
}