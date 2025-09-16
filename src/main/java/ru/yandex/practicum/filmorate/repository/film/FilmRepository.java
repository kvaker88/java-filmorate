package ru.yandex.practicum.filmorate.repository.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmRepository implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final MpaRowMapper mpaRowMapper;
    private final GenreRowMapper genreRowMapper;

    // ===== CRUD =====

    @Override
    @Transactional
    public void addFilm(Film film) {
        final String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        // жанры без дублей
        upsertFilmGenres(id, film.getGenres());

        // обогащение (MPA/жанры) не возвращаем — сервис при необходимости сам вызовет getFilmById
    }

    @Override
    @Transactional
    public void updateFilm(Film film) {
        final String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        // перезаписываем жанры (без дублей)
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        upsertFilmGenres(film.getId(), film.getGenres());
    }

    @Override
    public Film getFilmById(Long id) {
        Film base;
        try {
            base = jdbcTemplate.queryForObject(
                    "SELECT id, name, description, release_date, duration, mpa_id FROM films WHERE id = ?",
                    filmRowMapper, id
            );
        } catch (EmptyResultDataAccessException e) {
            return null; // сервис проверит doesFilmNotExist(...)
        }

        // mpa
        if (base.getMpa() != null && base.getMpa().getId() != null) {
            Mpa mpa = jdbcTemplate.queryForObject(
                    "SELECT id, name, description FROM mpa WHERE id = ?",
                    mpaRowMapper,
                    base.getMpa().getId()
            );
            base.setMpa(mpa);
        }

        // genres (отсортируем по id)
        List<Genre> genres = jdbcTemplate.query(
                "SELECT g.id, g.name FROM film_genres fg " +
                        "JOIN genres g ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ? ORDER BY g.id",
                genreRowMapper,
                id
        );
        base.setGenres(new LinkedHashSet<>(genres)); // в модели Set<Genre>
        return base;
    }

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query(
                "SELECT id, name, description, release_date, duration, mpa_id FROM films ORDER BY id",
                filmRowMapper
        );
        // обогащаем
        Map<Long, Mpa> mpaCache = new HashMap<>();
        for (Film f : films) {
            if (f.getMpa() != null && f.getMpa().getId() != null) {
                Long mpaId = f.getMpa().getId();
                Mpa mpa = mpaCache.computeIfAbsent(mpaId, key ->
                        jdbcTemplate.queryForObject("SELECT id, name, description FROM mpa WHERE id = ?",
                                mpaRowMapper, key));
                f.setMpa(mpa);
            }
            List<Genre> genres = jdbcTemplate.query(
                    "SELECT g.id, g.name FROM film_genres fg " +
                            "JOIN genres g ON g.id = fg.genre_id " +
                            "WHERE fg.film_id = ? ORDER BY g.id",
                    genreRowMapper, f.getId()
            );
            f.setGenres(new LinkedHashSet<>(genres)); // Set<Genre>
        }
        return films;
    }

    // ===== LIKES & POPULAR =====

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "INSERT INTO likes (film_id, user_id) " +
                        "SELECT ?, ? WHERE NOT EXISTS (" +
                        "    SELECT 1 FROM likes WHERE film_id = ? AND user_id = ?" +
                        ")",
                filmId, userId, filmId, userId
        );
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public boolean isLikeExists(Long filmId, Long userId) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class, filmId, userId
        );
        return cnt != null && cnt > 0;
    }

    @Override
    public Collection<Film> getPopularFilms(int limit) {
        final String sqlIds =
                "SELECT f.id " +
                        "FROM films f " +
                        "LEFT JOIN likes l ON l.film_id = f.id " +
                        "GROUP BY f.id " +
                        "ORDER BY COUNT(l.user_id) DESC, f.id ASC " +
                        "LIMIT ?";
        List<Long> ids = jdbcTemplate.query(sqlIds, (rs, rn) -> rs.getLong(1), limit);
        if (ids.isEmpty()) {
            return List.of();
        }
        // сохраняем порядок по популярности
        Map<Long, Integer> order = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            order.put(ids.get(i), i);
        }

        List<Film> films = ids.stream()
                .map(this::getFilmById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        films.sort(Comparator.comparingInt(f -> order.getOrDefault(f.getId(), Integer.MAX_VALUE)));
        return films;
    }

    // ===== EXISTS / DELETE =====

    @Override
    public boolean existsById(long id) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM films WHERE id = ?",
                Integer.class, id
        );
        return cnt != null && cnt > 0;
    }

    @Override
    public boolean doesFilmNotExist(Long id) {
        return (id == null) || !existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        // 1) связи фильм—жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", id);

        // 2) лайки к фильму
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ?", id);

        // 3) сам фильм
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    // ===== helpers =====

    private void upsertFilmGenres(Long filmId, Set<Genre> genres) {
        if (filmId == null || genres == null || genres.isEmpty()) return;

        // удалить дубликаты по id и сохранить порядок возрастания id
        LinkedHashMap<Long, Genre> uniq = new LinkedHashMap<>();
        for (Genre g : genres) {
            if (g != null && g.getId() != null) {
                uniq.put(g.getId(), g);
            }
        }
        for (Long gid : uniq.keySet()) {
            jdbcTemplate.update(
                    "INSERT INTO film_genres (film_id, genre_id) " +
                            "SELECT ?, ? WHERE NOT EXISTS (" +
                            "    SELECT 1 FROM film_genres WHERE film_id = ? AND genre_id = ?" +
                            ")",
                    filmId, gid, filmId, gid
            );
        }
    }
}