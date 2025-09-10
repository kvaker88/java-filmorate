package ru.yandex.practicum.filmorate.repository.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class MpaRowMapper implements RowMapper<Mpa> {
    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();

        try {
            mpa.setId(rs.getLong("mpa_id"));
        } catch (SQLException e) {
            mpa.setId(rs.getLong("id")); // fallback
        }

        try {
            mpa.setName(rs.getString("mpa_name"));
        } catch (SQLException e) {
            mpa.setName(rs.getString("name")); // fallback
        }

        try {
            String description = rs.getString("mpa_description");
            if (description != null) {
                mpa.setDescription(description);
            } else {
                mpa.setDescription(rs.getString("description")); // fallback
            }
        } catch (SQLException e) {
            log.warn("Ошибка при получении описания MPA рейтинга из результата запроса", e);
            mpa.setDescription(null);
        }

        return mpa;
    }
}
