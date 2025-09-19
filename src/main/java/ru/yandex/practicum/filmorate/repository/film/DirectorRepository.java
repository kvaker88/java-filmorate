package ru.yandex.practicum.filmorate.repository.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.BaseRepository;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class DirectorRepository extends BaseRepository<Director> implements DirectorStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM directors d ORDER BY d.id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String FIND_BY_FILM_QUERY = "SELECT d.* FROM directors d " +
            "JOIN film_directors fd ON d.id = fd.director_id WHERE fd.film_id = ? ORDER BY d.id";
    private static final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE id = ?";
    private static final String DELETE_BY_FILM_QUERY = "DELETE FROM film_directors WHERE film_id = ?";

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Director> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Director getById(Long directorId) {
        Director director = findOne(FIND_BY_ID_QUERY, directorId)
                .orElseThrow(() -> new NotFoundException("Режиссер с ID " + directorId + " не найден"));
        return director;
    }

    @Override
    public Director addDirector(Director director) {

        long id = insert(INSERT_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        update(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    @Override
    public boolean removeDirectorById(Long directorId) {
        return delete(DELETE_QUERY, directorId);
    }

    @Override
    public boolean doesDirectorExist(Long directorId) {
        Optional<Director> director = findOne(FIND_BY_ID_QUERY, directorId);
        return director.isPresent();
    }

    public void addDirectorsToFilm(Long filmId, Set<Director> directors) {
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
        jdbc.batchUpdate(sql, directors.stream()
                .map(director -> new Object[]{filmId, director.getId()})
                .toList());
    }

    public List<Director> findDirectorsByFilmId(Long filmId) {
        return findMany(FIND_BY_FILM_QUERY, filmId);
    }

    public void removeAllDirectorsFromFilm(Long filmId) {
        delete(DELETE_BY_FILM_QUERY, filmId);
    }
}
