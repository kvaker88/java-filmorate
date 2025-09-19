package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;
import ru.yandex.practicum.filmorate.validation.LikeValidator;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    public static final String FILM_NOT_FOUND = "Фильм с ID = %d не найден";

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getAllFilms() {
        log.info("Запрос на получение всех фильмов. Текущее количество: {}", filmStorage.getAllFilms().size());
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        log.info("Запрос создания фильма: {}", film);

        FilmValidator.validate(film);
        filmStorage.addFilm(film);

        log.info("Фильм успешно создан. ID: {}, Название: {}", film.getId(), film.getName());
        return film;
    }

    public Film updateFilm(Film film) {
        log.info("Запрос обновления фил ьма: {}", film);

        if (film.getId() == null) {
            log.warn("Ошибка при обновлении фильма: не указан ID");
            throw new ValidationException("ID фильма должен быть указан при обновлении");
        }

        if (filmStorage.doesFilmNotExist(film.getId())) {
            log.warn((String.format(FILM_NOT_FOUND, film.getId())));
            throw new NotFoundException(String.format(FILM_NOT_FOUND, film.getId()));
        }

        FilmValidator.validateForUpdate(film);
        filmStorage.updateFilm(film);
        log.info("Фильм с ID: {} успешно обновлен", film.getId());
        return film;
    }

    public Film getFilmById(Long filmId) {
        log.info("Запрос получения фильма по ID: {}", filmId);
        if (filmStorage.doesFilmNotExist(filmId)) {
            log.warn((String.format(FILM_NOT_FOUND, filmId)));
            throw new NotFoundException(String.format(FILM_NOT_FOUND, filmId));
        }
        return filmStorage.getFilmById(filmId);
    }

    public Film likeTheFilm(Long filmId, Long userId) {
        log.info("Запрос на лайк фильму = {}, от пользователя = {}", filmId, userId);
        LikeValidator.validate(filmId, userId, userStorage, filmStorage);

        if (filmStorage.isLikeExists(filmId, userId)) {
            throw new ValidationException("Лайк уже поставлен");
        }

        filmStorage.addLike(filmId, userId);
        log.info("Лайк добавлен");
        return filmStorage.getFilmById(filmId);
    }

    public Film dislikeFilm(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка фильму = {}, от пользователя = {}", filmId, userId);
        LikeValidator.validate(filmId, userId, userStorage, filmStorage);

        if (!filmStorage.isLikeExists(filmId, userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Лайк не найден");
        }

        filmStorage.deleteLike(filmId, userId);
        log.info("Лайк удалён");
        return filmStorage.getFilmById(filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        int limit = count > 0 ? count : 10;
        return filmStorage.getPopularFilms(limit);
    }

    public Collection<Film> getPopularFilms(int count, Long genreId, Integer year) {
        int limit = count > 0 ? count : 10;
        return filmStorage.getPopularFilms(limit, genreId, year);
    }

    public boolean isLikeExists(Long filmId, Long userId) {
        return filmStorage.isLikeExists(filmId, userId);
    }
}

