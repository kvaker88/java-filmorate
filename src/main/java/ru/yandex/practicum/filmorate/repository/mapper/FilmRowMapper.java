package ru.yandex.practicum.filmorate.repository.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final MpaRowMapper mpaRowMapper;

    public FilmRowMapper(MpaRowMapper mpaRowMapper) {
        this.mpaRowMapper = mpaRowMapper;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        java.sql.Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(rs.getLong("duration"));

        Mpa mpa = mpaRowMapper.mapRow(rs, rowNum);
        film.setMpa(mpa);

        // Инициализируем коллекции
        film.setGenres(new HashSet<>());
        film.setLikes(new HashSet<>());

        return film;
    }
}
