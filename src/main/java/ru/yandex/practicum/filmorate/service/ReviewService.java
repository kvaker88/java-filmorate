package ru.yandex.practicum.filmorate.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.review.ReviewRepository; // <-- правильный импорт

import java.util.List;

@Service
public class ReviewService {

    private static final int DEFAULT_COUNT = 10;

    private final ReviewRepository repository;

    public ReviewService(final ReviewRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Review create(final Review review) {
        try {
            return repository.create(review);
        } catch (final DataIntegrityViolationException e) {
            // Скорее всего нарушен FK (film_id/user_id)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Film or User not found for given IDs");
        }
    }

    @Transactional
    public Review update(final Review review) {
        // Проверяем существование
        final Review existing = getById(review.getReviewId());
        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        repository.update(existing);
        return getById(existing.getReviewId());
    }

    @Transactional
    public void delete(final Long reviewId) {
        // Бросим 404 если нет
        getById(reviewId);
        repository.delete(reviewId);
    }

    @Transactional(readOnly = true)
    public Review getById(final Long reviewId) {
        return repository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    @Transactional(readOnly = true)
    public List<Review> list(final Long filmId, final Integer count) {
        final int limit = (count == null || count <= 0) ? DEFAULT_COUNT : count;
        if (filmId == null) {
            return repository.findTop(limit);
        }
        return repository.findByFilm(filmId, limit);
    }

    @Transactional
    public void putVote(final Long reviewId, final Long userId, final int value) {
        if (value != 1 && value != -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vote must be 1 or -1");
        }
        // Проверим, что отзыв существует
        getById(reviewId);
        try {
            repository.upsertVote(reviewId, userId, value);
            repository.recalcUseful(reviewId);
        } catch (final DataIntegrityViolationException e) {
            // Нарушение FK (нет review или user)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review or User not found");
        }
    }

    @Transactional
    public void removeVote(final Long reviewId, final Long userId) {
        // Проверим, что отзыв существует
        getById(reviewId);
        repository.deleteVote(reviewId, userId);
        repository.recalcUseful(reviewId);
    }
}
