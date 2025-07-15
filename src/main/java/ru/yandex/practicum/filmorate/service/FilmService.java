package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmLikesResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;
import ru.yandex.practicum.filmorate.validation.LikeValidator;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getAllFilms() {
        log.info("Запрос на получение всех фильмов. Текущее количество: {}", filmStorage.getFilmsSize());
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        log.info("Запрос создания фильма: {}", film);
        FilmValidator.validate(film);

        film.setId(filmStorage.getNextId());
        filmStorage.addFilm(film);

        log.info("Фильм успешно создан. ID: {}, Название: {}", film.getId(), film.getName());
        return film;
    }

    public Film updateFilm(Film film) {
        log.info("Запрос обновления фильма: {}", film);

        if (film.getId() == null) {
            log.warn("Ошибка при обновлении фильма: не указан ID");
            throw new ValidationException("ID фильма должен быть указан при обновлении");
        }

        if (filmStorage.doesFilmNotExist(film.getId())) {
            log.warn((String.format("Фильм с ID = %d не найден", film.getId())));
            throw new NotFoundException(String.format("Фильм с ID = %d не найден", film.getId()));
        }

        FilmValidator.validate(film);
        filmStorage.addFilm(film);
        log.info("Фильм с ID: {} успешно обновлен", film.getId());
        return film;
    }

    public Film getFilmById(Long filmId) {
        log.info("Запрос получения фильма по ID: {}", filmId);
        if (filmStorage.doesFilmNotExist(filmId)) {
            log.warn((String.format("Фильм с ID = %d не найден", filmId)));
            throw new NotFoundException(String.format("Фильм с ID = %d не найден", filmId));
        }
        return filmStorage.getFilmById(filmId);
    }

    public Film likeTheFilm(Long filmId, Long userId) {
        log.info("Запрос на лайк фильму = {}, от пользователя = {}", filmId, userId);
        LikeValidator.validate(filmId, userId, userStorage, filmStorage);
        Film film = filmStorage.getFilmById(filmId);
        film.addLike(userId);
        log.info("Лайк поставлен");
        return film;
    }

    public Film dislikeFilm(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка фильму = {}, от пользователя = {}", filmId, userId);
        LikeValidator.validate(filmId, userId, userStorage, filmStorage);
        Film film = filmStorage.getFilmById(filmId);

        if (!film.getLikes().contains(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Лайк не найден");
        }

        film.deleteLike(userId);
        log.info("Лайк удалён");
        return film;
    }

    public Collection<FilmLikesResponse> getPopularFilms(int count) {
        log.info("Запрос на получение популярных фильмов");
        return filmStorage.getAllFilms().stream()
                .map(film -> new FilmLikesResponse(
                        film.getId(),
                        film.getName(),
                        film.getLikes().size()
                ))
                .sorted(Comparator.comparingInt(FilmLikesResponse::getLikesCount).reversed())
                .limit(Math.max(count, 1))
                .collect(Collectors.toList());
    }
}

