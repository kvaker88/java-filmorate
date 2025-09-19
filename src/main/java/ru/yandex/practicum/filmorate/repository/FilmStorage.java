package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> getAllFilms();

    Film getFilmById(Long filmId);

    void addFilm(Film film);

    void updateFilm(Film film);

    boolean doesFilmNotExist(Long id);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(int count);

    Collection<Film> getPopularFilms(int count, Long genre, Integer year);

    boolean isLikeExists(Long filmId, Long userId);

    List<Long> getUserLikedFilmIds(Long userId);

    List<Long> getUsersWhoLikedFilm(Long filmId);

    Map<Long, Set<Long>> getAllUserLikes();

    int getCommonLikesCount(Long userId1, Long userId2);

    List<Film> getFilmsByIds(List<Long> filmIds);
}
