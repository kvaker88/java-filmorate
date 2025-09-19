package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAllFilms();

    Film getFilmById(Long filmId);

    void addFilm(Film film);

    void updateFilm(Film film);

    boolean doesFilmNotExist(Long id);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(int count);

    boolean isLikeExists(Long filmId, Long userId);
}
