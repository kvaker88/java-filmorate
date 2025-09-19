package ru.yandex.practicum.filmorate.service;

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
import ru.yandex.practicum.filmorate.repository.user.UserRepository;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmRepository.class, UserRepository.class, GenreRepository.class, MpaRepository.class, FilmService.class,
        UserService.class, FilmController.class, UserController.class, FilmRowMapper.class, UserRowMapper.class,
        MpaRowMapper.class, GenreRowMapper.class, RecommendationService.class})
class FilmServiceTest {
    @Autowired
    private FilmService filmService;
    @Autowired
    private UserService userService;
    private Film testFilm;
    private User validUser;

    @BeforeEach
    void setUp() {
        testFilm = new Film(
                null,
                "Валидный Фильм",
                "Валидное Описание",
                LocalDate.of(2000, 1, 1),
                120L
        );
        validUser = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );
    }

    @Test
    @DisplayName("Создание фильма с валидными данными → успешно создает фильм")
    void createFilm_withValidData_shouldCreateFilm() {
        Film createdFilm = filmService.createFilm(testFilm);

        assertNotNull(createdFilm.getId());
        assertEquals(1L, createdFilm.getId());
        assertEquals("Валидный Фильм", createdFilm.getName());
    }

    @Test
    @DisplayName("Создание фильма с пустым названием → исключение ValidationException")
    void createFilm_withEmptyName_shouldThrowValidationException() {
        Film film = new Film(
                null,
                "",
                "Описание",
                LocalDate.now(),
                120L
        );

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.createFilm(film));
        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    @DisplayName("Создание фильма с описанием длиннее 200 символов → исключение ValidationException")
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
                () -> filmService.createFilm(film));
        assertTrue(exception.getMessage().contains("Описание не может превышать"));
    }

    @Test
    @DisplayName("Создание фильма с датой релиза раньше 28.12.1895 → исключение ValidationException")
    void createFilm_withEarlyReleaseDate_shouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.of(1895, 12, 27),
                120L
        );

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.createFilm(film));
        assertTrue(exception.getMessage().contains("Дата релиза не может быть раньше"));
    }

    @Test
    @DisplayName("Создание фильма с отрицательной продолжительностью → исключение ValidationException")
    void createFilm_withNegativeDuration_shouldThrowValidationException() {
        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.now(),
                -1L
        );

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.createFilm(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    @DisplayName("Обновление фильма с несуществующим ID → исключение NotFoundException")
    void updateFilm_withNonExistingId_shouldThrowNotFoundException() {
        Film film = new Film(
                999L,
                "Фильм",
                "Описание",
                LocalDate.now(),
                120L
        );

        assertThrows(NotFoundException.class, () -> filmService.updateFilm(film));
    }

    @Test
    @DisplayName("Обновление фильма с валидными данными → успешное обновление")
    void updateFilm_withValidData_shouldUpdateFilm() {
        Film createdFilm = filmService.createFilm(testFilm);
        Film updateData = new Film(
                createdFilm.getId(),
                "Новый Фильм",
                "Новое Описание",
                LocalDate.of(2001, 1, 1),
                90L
        );

        Film updatedFilm = filmService.updateFilm(updateData);

        assertEquals("Новый Фильм", updatedFilm.getName());
        assertEquals("Новое Описание", updatedFilm.getDescription());
        assertEquals(90L, updatedFilm.getDuration());
    }

    @Test
    @DisplayName("Получение всех фильмов → возвращает список всех созданных фильмов")
    void getAllFilms_shouldReturnAllFilms() {
        filmService.createFilm(testFilm);
        Film anotherFilm = new Film(
                null,
                "Другой Фильм",
                "Другое Описание",
                LocalDate.now(),
                90L
        );

        filmService.createFilm(anotherFilm);
        assertEquals(2, filmService.getAllFilms().size());
    }

    @Test
    @DisplayName("Получение фильма по несуществующему ID → выбрасывает NotFoundException")
    void getFilmById_withNonExistingId_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> filmService.getFilmById(999L));
    }

    @Test
    @DisplayName("Добавление лайка к несуществующему фильму → исключение NotFoundException")
    void likeTheFilm_withNonExistingFilm_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> filmService.likeTheFilm(1L, 1L));
    }

    @Test
    @DisplayName("Добавление лайка с валидными ID → успешное добавление")
    void likeTheFilm_withValidData_shouldAddLike() {
        filmService.createFilm(testFilm);
        userService.createUser(validUser);

        filmService.likeTheFilm(testFilm.getId(), validUser.getId());

        assertTrue(filmService.isLikeExists(testFilm.getId(), validUser.getId()));
    }

    @Test
    @DisplayName("Удаление несуществующего лайка → исключение ValidationException")
    void dislikeFilm_whenLikeNotExists_shouldThrowValidationException() {
        filmService.createFilm(testFilm);
        userService.createUser(validUser);

        assertThrows(ValidationException.class, () -> filmService.dislikeFilm(1L, 1L));
    }

    @Test
    @DisplayName("Удаление лайка → успешное удаление")
    void dislikeFilm_withExistingLike_shouldRemoveLike() {
        filmService.createFilm(testFilm);
        userService.createUser(validUser);

        filmService.likeTheFilm(1L, 1L);

        assertTrue(filmService.isLikeExists(testFilm.getId(), validUser.getId()));
    }

    @Test
    @DisplayName("Получение популярных фильмов из пустого хранилища → возвращает пустой список")
    void getPopularFilms_withEmptyStorage_shouldReturnEmptyList() {
        Collection<Film> result = filmService.getPopularFilms(10);

        assertTrue(result.isEmpty());
    }
}