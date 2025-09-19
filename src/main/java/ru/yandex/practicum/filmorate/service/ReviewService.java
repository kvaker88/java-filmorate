package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.review.ReviewRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository repository;

    public Review create(@Valid Review review) {
        return repository.save(review);
    }

    public Review update(@Valid Review review) {
        if (review.getReviewId() == null || !repository.existsById(review.getReviewId())) {
            throw new NotFoundException("Отзыв не найден: id=" + review.getReviewId());
        }
        return repository.update(review);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Отзыв не найден: id=" + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Review getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Review> getReviews(Long filmId, int count) {
        if (filmId == null) return repository.findAllOrderByUseful(count);
        return repository.findAllByFilmIdOrderByUseful(filmId, count);
    }

    public void like(Long reviewId, Long userId) {
        ensureExists(reviewId);
        repository.react(reviewId, userId, true);
    }

    public void dislike(Long reviewId, Long userId) {
        ensureExists(reviewId);
        repository.react(reviewId, userId, false);
    }

    public void removeLike(Long reviewId, Long userId) {
        ensureExists(reviewId);
        repository.removeReaction(reviewId, userId, true);
    }

    public void removeDislike(Long reviewId, Long userId) {
        ensureExists(reviewId);
        repository.removeReaction(reviewId, userId, false);
    }

    private void ensureExists(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Отзыв не найден: id=" + id);
        }
    }
}

