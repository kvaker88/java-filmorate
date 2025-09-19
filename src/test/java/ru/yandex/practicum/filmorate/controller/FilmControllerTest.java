package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.film.DirectorRepository;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.film.GenreRepository;
import ru.yandex.practicum.filmorate.repository.film.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mapper.*;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmRepository.class, UserRepository.class, GenreRepository.class, MpaRepository.class, FilmService.class,
        UserService.class, FilmController.class, UserController.class, FilmRowMapper.class, UserRowMapper.class,
        MpaRowMapper.class, GenreRowMapper.class, DirectorRepository.class, DirectorService.class,
        DirectorController.class, DirectorRowMapper.class})
class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserController userController;

    private Film testFilm;
    private User validUser;
    @Autowired
    private FilmRepository filmRepository;

    @BeforeEach
    void setUp() {
        testFilm = new Film(
                null,
                "Фильм",
                "ОписаниеФильма",
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

        testFilm = filmController.createFilm(testFilm);
        validUser = userController.createUser(validUser);
    }

    @Test
    @DisplayName("Создание фильма с валидными данными → возвращает фильм с ID")
    void createFilm_withValidData_shouldReturnFilmWithId() {
        Film newFilm = new Film(
                null,
                "НовыйФильм",
                "НовоеОписание",
                LocalDate.of(2020, 1, 1),
                90L
        );

        Film createdFilm = filmController.createFilm(newFilm);

        assertNotNull(createdFilm.getId());
        assertEquals("НовыйФильм", createdFilm.getName());
    }

    @Test
    @DisplayName("Создание фильма с невалидной продолжительностью → исключение ValidationException")
    void createFilm_withInvalidDuration_shouldThrowValidationException() {
        Film intestFilm = new Film(
                null,
                "НекорректныйФильм",
                "ОписаниеФильма",
                LocalDate.now(),
                -90L
        );

        assertThrows(ValidationException.class, () -> filmController.createFilm(intestFilm));
    }

    @Test
    @DisplayName("Получение фильма по несуществующему ID → исключение NotFoundException")
    void getFilmById_withNonExistingId_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> filmController.getUserById(9999L));
    }

    @Test
    @DisplayName("Получение всех фильмов → возвращает список всех созданных фильмов")
    void getAllFilms_shouldReturnAllCreatedFilms() {
        Collection<Film> films = filmController.getAllFilms();

        assertEquals(1, films.size());
        assertTrue(films.stream().anyMatch(f -> f.getName().equals("Фильм")));
    }

    @Test
    @DisplayName("Обновление фильма с валидными данными → успешное обновление")
    void updateFilm_withValidData_shouldUpdateFilm() {
        testFilm.setDescription("НовоеОписаниеФильма");

        Film updatedFilm = filmController.updateFilm(testFilm);

        assertEquals("НовоеОписаниеФильма", updatedFilm.getDescription());
        assertEquals(testFilm.getId(), updatedFilm.getId());
    }

    @Test
    @DisplayName("Добавление лайка с валидными ID → успешно добавляет лайк")
    void likeTheFilm_withValidIds_shouldAddLike() {
        Film filmWithLike = filmController.likeTheFilm(testFilm.getId(), validUser.getId());

        boolean likeExists = filmRepository.isLikeExists(testFilm.getId(), validUser.getId());
        assertTrue(likeExists);

        assertNotNull(filmWithLike);
        assertEquals(testFilm.getId(), filmWithLike.getId());
    }

    @Test
    @DisplayName("Удаление лайка → успешно удаляет лайк")
    void dislikeFilm_shouldRemoveLike() {
        filmController.likeTheFilm(testFilm.getId(), validUser.getId());

        assertTrue(filmRepository.isLikeExists(testFilm.getId(), validUser.getId()));

        Film filmWithoutLike = filmController.dislikeFilm(testFilm.getId(), validUser.getId());

        boolean likeExists = filmRepository.isLikeExists(testFilm.getId(), validUser.getId());
        assertFalse(likeExists);

        assertNotNull(filmWithoutLike);
        assertEquals(testFilm.getId(), filmWithoutLike.getId());
    }

    @Test
    @DisplayName("Получение популярных фильмов → возвращает список, отсортированный по количеству лайков")
    void getPopularFilms_shouldReturnOrderedByLikes() {
        User user2 = userController.createUser(new User(
                null,
                "mail2@yandex.ru",
                "ДругойЛогин",
                "ДругоеИмя",
                LocalDate.of(1995, 1, 1))
        );

        Film film2 = filmController.createFilm(new Film(
                null,
                "ДругойФильм",
                "ДругоеОписание",
                LocalDate.of(2010, 1, 1),
                100L)
        );

        filmController.likeTheFilm(film2.getId(), validUser.getId());
        filmController.likeTheFilm(film2.getId(), user2.getId());
        filmController.likeTheFilm(testFilm.getId(), validUser.getId());

        Collection<Film> popularFilms = filmController.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(film2.getId(), popularFilms.iterator().next().getId());
    }
}
