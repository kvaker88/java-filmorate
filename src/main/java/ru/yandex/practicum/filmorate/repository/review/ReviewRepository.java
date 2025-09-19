package ru.yandex.practicum.filmorate.repository.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.mapper.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {

    private final JdbcTemplate jdbc;
    private final ReviewRowMapper mapper = new ReviewRowMapper();

    private static final String BASE_SELECT = """
        SELECT r.review_id,
               r.content,
               r.is_positive,
               r.user_id,
               r.film_id,
               COALESCE(SUM(CASE WHEN rr.is_like THEN 1 ELSE -1 END), 0) AS useful
        FROM reviews r
        LEFT JOIN review_reactions rr ON rr.review_id = r.review_id
        """;

    private static final String GROUP_ORDER = """
        GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id
        ORDER BY useful DESC
        """;

    public Review save(Review review) {
        final String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?,?,?,?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, kh);
        Long id = kh.getKey().longValue();
        review.setReviewId(id);
        review.setUseful(0);
        return review;
    }

    public Optional<Review> findById(Long id) {
        final String sql = BASE_SELECT + " WHERE r.review_id = ? " + GROUP_ORDER;
        List<Review> list = jdbc.query(sql, mapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Review update(Review review) {
        final String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbc.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId()).orElse(null);
    }

    public void deleteById(Long id) {
        // Сначала реакции, потом отзыв (на случай отсутствия ON DELETE CASCADE)
        jdbc.update("DELETE FROM review_reactions WHERE review_id = ?", id);
        jdbc.update("DELETE FROM reviews WHERE review_id = ?", id);
    }

    public boolean existsById(Long id) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM reviews WHERE review_id = ?", Integer.class, id);
        return n != null && n > 0;
    }

    public List<Review> findAllByFilmIdOrderByUseful(Long filmId, int count) {
        final String sql = BASE_SELECT + " WHERE r.film_id = ? " + GROUP_ORDER + " LIMIT ?";
        return jdbc.query(sql, mapper, filmId, count);
    }

    public List<Review> findAllOrderByUseful(int count) {
        final String sql = BASE_SELECT + GROUP_ORDER + " LIMIT ?";
        return jdbc.query(sql, mapper, count);
    }

    /** upsert реакции: лайк/дизлайк. */
    public void react(Long reviewId, Long userId, boolean like) {
        // H2 MERGE — безопасно для CI
        final String sql = """
            MERGE INTO review_reactions (review_id, user_id, is_like)
            KEY (review_id, user_id)
            VALUES (?, ?, ?)
            """;
        jdbc.update(sql, reviewId, userId, like);
    }

    public void removeReaction(Long reviewId, Long userId, boolean like) {
        final String sql = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ? AND is_like = ?";
        jdbc.update(sql, reviewId, userId, like);
    }
}

