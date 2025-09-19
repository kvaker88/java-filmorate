package ru.yandex.practicum.filmorate.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository {
    private final JdbcTemplate jdbc;

    public ReviewRepository(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Review create(final Review r) {
        final String sql = "INSERT INTO reviews (film_id, user_id, content, is_positive, useful) VALUES (?,?,?,?,0)";
        final KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            final PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, r.getFilmId());
            ps.setLong(2, r.getUserId());
            ps.setString(3, r.getContent());
            ps.setBoolean(4, r.getIsPositive());
            return ps;
        }, kh);
        final Number key = kh.getKey();
        if (key != null) {
            r.setReviewId(key.longValue());
        }
        r.setUseful(0);
        return r;
    }

    public void update(final Review r) {
        final String sql = "UPDATE reviews SET content=?, is_positive=? WHERE review_id=?";
        jdbc.update(sql, r.getContent(), r.getIsPositive(), r.getReviewId());
    }

    public void delete(final Long reviewId) {
        // votes are FK ON DELETE CASCADE; if not, delete manually
        jdbc.update("DELETE FROM reviews WHERE review_id=?", reviewId);
    }

    public Optional<Review> findById(final Long reviewId) {
        try {
            final String sql = "SELECT review_id, film_id, user_id, content, is_positive, useful FROM reviews WHERE review_id=?";
            final Review r = jdbc.queryForObject(sql, mapper, reviewId);
            return Optional.ofNullable(r);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Review> findByFilm(final Long filmId, final int limit) {
        final String sql = """
                SELECT review_id, film_id, user_id, content, is_positive, useful
                FROM reviews
                WHERE film_id=?
                ORDER BY useful DESC, review_id ASC
                LIMIT ?
                """;
        return jdbc.query(sql, mapper, filmId, limit);
    }

    public List<Review> findTop(final int limit) {
        final String sql = """
                SELECT review_id, film_id, user_id, content, is_positive, useful
                FROM reviews
                ORDER BY useful DESC, review_id ASC
                LIMIT ?
                """;
        return jdbc.query(sql, mapper, limit);
    }

    /**
     * Insert or update vote (value in {1, -1}). Works on both H2 and Postgres without vendor-specific UPSERT.
     */
    public void upsertVote(final Long reviewId, final Long userId, final int value) {
        final String updateSql = "UPDATE review_votes SET value=? WHERE review_id=? AND user_id=?";
        final int updated = jdbc.update(updateSql, value, reviewId, userId);
        if (updated == 0) {
            final String insertSql = "INSERT INTO review_votes (review_id, user_id, value) VALUES (?,?,?)";
            jdbc.update(insertSql, reviewId, userId, value);
        }
    }

    public void deleteVote(final Long reviewId, final Long userId) {
        jdbc.update("DELETE FROM review_votes WHERE review_id=? AND user_id=?", reviewId, userId);
    }

    /** Recalculate and store current useful = sum(value) for the review. */
    public void recalcUseful(final Long reviewId) {
        final String sql = """
            UPDATE reviews r
            SET useful = COALESCE((SELECT SUM(v.value) FROM review_votes v WHERE v.review_id = r.review_id), 0)
            WHERE r.review_id = ?
            """;
        jdbc.update(sql, reviewId);
    }

    private final RowMapper<Review> mapper = (rs, rowNum) -> {
        final Review r = new Review();
        r.setReviewId(rs.getLong("review_id"));
        r.setFilmId(rs.getLong("film_id"));
        r.setUserId(rs.getLong("user_id"));
        r.setContent(rs.getString("content"));
        r.setIsPositive(rs.getBoolean("is_positive"));
        r.setUseful(rs.getInt("useful"));
        return r;
    };
}
