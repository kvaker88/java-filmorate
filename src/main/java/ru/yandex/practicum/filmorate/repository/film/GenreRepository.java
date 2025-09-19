package ru.yandex.practicum.filmorate.repository.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_BY_FILM_QUERY =
            "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.id";
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> genreRowMapper) {
        super(jdbc, genreRowMapper);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Genre> findGenresByFilmId(Long filmId) {
        return findMany(FIND_BY_FILM_QUERY, filmId);
    }

    public void addGenresToFilm(Long filmId, Set<Genre> genres) {
        if (genres != null && !genres.isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            jdbc.batchUpdate(sql, genres.stream()
                    .map(genre -> new Object[]{filmId, genre.getId()})
                    .toList());
        }
    }

    public void removeAllGenresFromFilm(Long filmId) {
        jdbc.update(DELETE_GENRES_QUERY, filmId);
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return (count > 0);
    }
}
