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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Primary
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_QUERY = "" +
            "SELECT f.*, " +
            "m.id as mpa_id, " +
            "m.name as mpa_name " +
            "FROM films f " +
            "JOIN mpa m ON f.mpa_id = m.id";
    private static final String FIND_BY_ID_QUERY = "" +
            "SELECT f.*, " +
            "m.id as mpa_id, " +
            "m.name as mpa_name " +
            "FROM films f " +
            "JOIN mpa m ON f.mpa_id = m.id " +
            "WHERE f.id = ?";
    private static final String INSERT_QUERY = "" +
            "INSERT INTO films " +
            "(name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "" +
            "UPDATE films " +
            "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM films";
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
    public Long getFilmsSize() {
        return jdbc.queryForObject(COUNT_QUERY, Long.class);
    }

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(film -> {
            loadFilmGenres(film);
            loadFilmLikes(film);
        });
        return films;
    }

    @Override
    public Film getFilmById(Long filmId) {
        Film film = findOne(FIND_BY_ID_QUERY, filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
        loadFilmGenres(film);
        loadFilmLikes(film);
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

    private void loadFilmLikes(Film film) {
        if (film != null) {
            String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
            List<Long> likes = jdbc.query(sql, (rs, rowNum) -> rs.getLong("user_id"), film.getId());
            film.setLikes(new HashSet<>(likes));
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
}