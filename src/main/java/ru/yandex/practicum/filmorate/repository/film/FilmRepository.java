package ru.yandex.practicum.filmorate.repository.film;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.BaseRepository;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;

import java.util.*;

@Repository
@Primary
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY =
            "SELECT f.*, " +
                    "m.id as mpa_id, " +
                    "m.name as mpa_name " +
                    "FROM films f " +
                    "JOIN mpa m ON f.mpa_id = m.id";
    private static final String FIND_BY_ID_QUERY =
            "SELECT f.*, " +
                    "m.id as mpa_id, " +
                    "m.name as mpa_name " +
                    "FROM films f " +
                    "JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE f.id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO films " +
                    "(name, description, release_date, duration, mpa_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE films " +
                    "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String EXISTS_QUERY = "SELECT COUNT(*) FROM films WHERE id = ?";

    private final GenreRepository genreRepository;

    public FilmRepository(JdbcTemplate jdbc,
                          FilmRowMapper filmRowMapper,
                          GenreRepository genreRepository
    ) {
        super(jdbc, filmRowMapper);
        this.genreRepository = genreRepository;
    }

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(this::loadFilmGenres);
        return films;
    }

    @Override
    public Film getFilmById(Long filmId) {
        Film film = findOne(FIND_BY_ID_QUERY, filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
        loadFilmGenres(film);
        return film;
    }

    @Override
    public void addFilm(Film film) {
        Long mpaId = (film.getMpa() != null) ? film.getMpa().getId() : 1;

        if (film.getMpa() != null) {
            validateMpaExists(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            validateAllGenresExist(film.getGenres());
        }

        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId);
        film.setId(id);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(film);
        }
    }

    private void validateMpaExists(Long mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mpaId);

        if (count == 0) {
            throw new NotFoundException("MPA рейтинг с ID " + mpaId + " не найден");
        }
    }

    private void validateAllGenresExist(Set<Genre> genres) {
        for (Genre genre : genres) {
            if (genre != null && genre.getId() != null) {
                validateGenreExists(genre.getId());
            }
        }
    }

    private void validateGenreExists(Long genreId) {
        String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, genreId);

        if (count == null || count == 0) {
            throw new NotFoundException("Жанр с ID " + genreId + " не найден");
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count == null || count == 0) {
            jdbc.update(INSERT_LIKE_QUERY, filmId, userId);
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
    }

    @Override
    public void updateFilm(Film film) {
        Long mpaId = (film.getMpa() != null) ? film.getMpa().getId() : 1;
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId());
        updateFilmGenres(film);
    }

    @Override
    public boolean doesFilmNotExist(Long id) {
        Integer count = jdbc.queryForObject(EXISTS_QUERY, Integer.class, id);
        return count == 0;
    }

    private void loadFilmGenres(Film film) {
        if (film != null) {
            film.setGenres(new HashSet<>(genreRepository.findGenresByFilmId(film.getId())));
        }
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreRepository.addGenresToFilm(film.getId(), film.getGenres());
        }
    }

    private void updateFilmGenres(Film film) {
        genreRepository.removeAllGenresFromFilm(film.getId());
        saveFilmGenres(film);
    }

    public Collection<Film> getPopularFilms(int count) {
        String sql = """
                    SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description
                    FROM films f
                    JOIN mpa m ON f.mpa_id = m.id
                    LEFT JOIN (
                        SELECT film_id, COUNT(user_id) as likes_count
                        FROM film_likes
                        GROUP BY film_id
                    ) l ON f.id = l.film_id
                    ORDER BY l.likes_count DESC NULLS LAST, f.id ASC
                    LIMIT ?
                """;

        List<Film> films = findMany(sql, count);
        films.forEach(this::loadFilmGenres);
        return films;
    }

    public Collection<Film> getPopularFilms(int count, Long genreId, Integer year) {
        StringBuilder sqlBuilder = new StringBuilder("""
        SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description
        FROM films f
        JOIN mpa m ON f.mpa_id = m.id
        LEFT JOIN (
            SELECT film_id, COUNT(user_id) as likes_count
            FROM film_likes
            GROUP BY film_id
        ) l ON f.id = l.film_id
        """);
        List<Object> params = new ArrayList<>();
        List<String> whereConditions = new ArrayList<>();

        if (genreId != null) {
            sqlBuilder.append("JOIN film_genres fg ON f.id = fg.film_id ");
            whereConditions.add("fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null) {
            whereConditions.add("EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        if (!whereConditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", whereConditions)).append(" ");
        }

        sqlBuilder.append("ORDER BY l.likes_count DESC NULLS LAST, f.id ASC LIMIT ?");
        params.add(count);

        List<Film> films = findMany(sqlBuilder.toString(), params.toArray());
        films.forEach(this::loadFilmGenres);
        return films;
    }

    @Override
    public boolean isLikeExists(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    public int getLikesCount(Long filmId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, filmId);
        return count != null ? count : 0;
    }

    public List<Long> getUserLikedFilmIds(Long userId) {
        String sql = "SELECT film_id FROM film_likes WHERE user_id = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("film_id"), userId);
    }

    public List<Long> getUsersWhoLikedFilm(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }

    public Map<Long, Set<Long>> getAllUserLikes() {
        String sql = "SELECT user_id, film_id FROM film_likes ORDER BY user_id";
        Map<Long, Set<Long>> userLikes = new HashMap<>();

        jdbc.query(sql, rs -> {
            Long userId = rs.getLong("user_id");
            Long filmId = rs.getLong("film_id");

            userLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        });

        return userLikes;
    }

    public int getCommonLikesCount(Long userId1, Long userId2) {
        String sql = """
        SELECT COUNT(*) FROM film_likes fl1 
        JOIN film_likes fl2 ON fl1.film_id = fl2.film_id 
        WHERE fl1.user_id = ? AND fl2.user_id = ?
        """;
        Integer count = jdbc.queryForObject(sql, Integer.class, userId1, userId2);
        return count != null ? count : 0;
    }

    public List<Film> getFilmsByIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format("""
        SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description
        FROM films f
        JOIN mpa m ON f.mpa_id = m.id
        WHERE f.id IN (%s)
        """, placeholders);

        List<Film> films = findMany(sql, filmIds.toArray());
        films.forEach(this::loadFilmGenres);

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = """
        SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
        FROM films f
        JOIN mpa m ON f.mpa_id = m.id
        WHERE f.id IN
            (SELECT films_id.film_id
             FROM (SELECT f1.film_id
                   FROM film_likes f1
                   INNER JOIN film_likes f2 ON f1.film_id = f2.film_id
                   WHERE f1.user_id = ? AND f2.user_id = ?) films_id
             INNER JOIN film_likes f3 ON films_id.film_id = f3.film_id
             GROUP BY films_id.film_id
             ORDER BY COUNT(f3.user_id) DESC NULLS LAST)""";

        List<Film> films = findMany(sql, userId, friendId);
        films.forEach(this::loadFilmGenres);


        return films;
    }
}