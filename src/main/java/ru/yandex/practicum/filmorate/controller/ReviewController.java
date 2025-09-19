package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.review.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(final ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@Valid @RequestBody final Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody final Review review) {
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") final Long reviewId) {
        reviewService.delete(reviewId);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable("id") final Long reviewId) {
        return reviewService.getById(reviewId);
    }

    @GetMapping
    public List<Review> list(@RequestParam(value = "filmId", required = false) final Long filmId,
                             @RequestParam(value = "count", required = false) final Integer count) {
        return reviewService.list(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable("id") final Long reviewId,
                     @PathVariable("userId") final Long userId) {
        reviewService.putVote(reviewId, userId, 1);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable("id") final Long reviewId,
                        @PathVariable("userId") final Long userId) {
        reviewService.putVote(reviewId, userId, -1);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") final Long reviewId,
                           @PathVariable("userId") final Long userId) {
        reviewService.removeVote(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable("id") final Long reviewId,
                              @PathVariable("userId") final Long userId) {
        reviewService.removeVote(reviewId, userId);
    }
}

