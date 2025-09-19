package ru.yandex.practicum.filmorate.repository.film;

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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmRepository.class, UserRepository.class, GenreRepository.class, MpaRepository.class,
        FilmService.class, UserService.class, FilmController.class, UserController.class,
        FilmRowMapper.class, UserRowMapper.class, MpaRowMapper.class, GenreRowMapper.class})
class FilmRepositoryTest {

    @Autowired
    private FilmRepository filmRepository;

    @Autowired
    private UserRepository userRepository;

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

        userRepository.addUser(validUser);
        filmRepository.addFilm(testFilm);
    }

    @Test
    @DisplayName("Получение всех фильмов → возвращает непустую коллекцию")
    void getAllFilms_shouldReturnNonEmptyCollection() {
        Collection<Film> films = filmRepository.getAllFilms();

        assertNotNull(films);
        assertEquals(1, films.size());
    }

    @Test
    @DisplayName("Получение фильма по несуществующему ID → исключение NotFoundException")
    void getFilmById_withNonExistingId_shouldThrowNotFoundException() {
        Long nonExistingId = 9999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmRepository.getFilmById(nonExistingId)
        );

        assertTrue(exception.getMessage().contains("Фильм с ID " + nonExistingId + " не найден"));
    }

    @Test
    @DisplayName("Добавление фильма с валидными данными → успешно сохраняет фильм")
    void addFilm_withValidData_shouldSaveFilm() {
        Film newFilm = new Film(
                null,
                "НовыйФильм",
                "НовоеОписание",
                LocalDate.of(2022, 1, 1),
                100L
        );

        filmRepository.addFilm(newFilm);

        assertNotNull(newFilm.getId());
        Film savedFilm = filmRepository.getFilmById(newFilm.getId());
        assertEquals("НовыйФильм", savedFilm.getName());
    }

    @Test
    @DisplayName("Добавление лайка фильму → успешно добавляет лайк")
    void addLike_shouldAddLikeToFilm() {
        filmRepository.addLike(testFilm.getId(), validUser.getId());

        boolean likeExists = filmRepository.isLikeExists(testFilm.getId(), validUser.getId());

        assertTrue(likeExists);
    }

    @Test
    @DisplayName("Добавление дублирующего лайка → не создает дубликат")
    void addLike_duplicateLike_shouldNotCreateDuplicate() {
        filmRepository.addLike(testFilm.getId(), validUser.getId());
        filmRepository.addLike(testFilm.getId(), validUser.getId()); // Дублирующий лайк

        int likesCount = filmRepository.getLikesCount(testFilm.getId());
        assertEquals(1, likesCount); // Всего один лайк
    }

    @Test
    @DisplayName("Удаление лайка → успешно удаляет лайк")
    void deleteLike_shouldRemoveLikeFromFilm() {
        filmRepository.addLike(testFilm.getId(), validUser.getId());

        filmRepository.deleteLike(testFilm.getId(), validUser.getId());

        boolean likeExists = filmRepository.isLikeExists(testFilm.getId(), validUser.getId());
        int likesCount = filmRepository.getLikesCount(testFilm.getId());

        assertFalse(likeExists);
        assertEquals(0, likesCount);
    }

    @Test
    @DisplayName("Удаление несуществующего лайка → не вызывает ошибок")
    void deleteLike_nonExistingLike_shouldNotThrowError() {
        assertDoesNotThrow(() -> filmRepository.deleteLike(testFilm.getId(), validUser.getId()));
    }

    @Test
    @DisplayName("Обновление фильма → успешно обновляет данные")
    void updateFilm_shouldUpdateFilmData() {
        testFilm.setName("ОбновлённоеИмя");
        testFilm.setDescription("ОбновлённоеОписание");
        testFilm.setDuration(150L);

        filmRepository.updateFilm(testFilm);

        Film updatedFilm = filmRepository.getFilmById(testFilm.getId());
        assertEquals("ОбновлённоеИмя", updatedFilm.getName());
        assertEquals("ОбновлённоеОписание", updatedFilm.getDescription());
        assertEquals(150L, updatedFilm.getDuration());
    }

    @Test
    @DisplayName("Проверка существования фильма → возвращает false для существующего фильма")
    void doesFilmNotExist_withExistingFilm_shouldReturnFalse() {
        boolean result = filmRepository.doesFilmNotExist(testFilm.getId());
        assertFalse(result);
    }

    @Test
    @DisplayName("Проверка существования фильма → возвращает true для несуществующего фильма")
    void doesFilmNotExist_withNonExistingFilm_shouldReturnTrue() {
        boolean result = filmRepository.doesFilmNotExist(9999L);
        assertTrue(result);
    }

    @Test
    @DisplayName("Добавление фильма с несуществующим жанром → исключение NotFoundException")
    void addFilm_withNonExistingGenre_shouldThrowNotFoundException() {
        Genre nonExistingGenre = new Genre(9999L, "Non-Existing");
        Set<Genre> genres = new HashSet<>();
        genres.add(nonExistingGenre);

        Film film = new Film(
                null,
                "Фильм",
                "Описание",
                LocalDate.of(2022, 1, 1),
                100L
        );
        film.setGenres(genres);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmRepository.addFilm(film)
        );

        assertTrue(exception.getMessage().contains("Жанр с ID 9999 не найден"));
    }
}