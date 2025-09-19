package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;


public class Review {

    // JSON: reviewId
    private Long reviewId;

    @NotBlank
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Long userId;  // author user id

    @NotNull
    private Long filmId;  // film id

    // calculated/useful rating (likes - dislikes). Kept as an integer field in DB.
    private Integer useful = 0;

    public Review() {
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(final Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public Boolean getIsPositive() {
        return isPositive;
    }

    public void setIsPositive(final Boolean isPositive) {
        this.isPositive = isPositive;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public Long getFilmId() {
        return filmId;
    }

    public void setFilmId(final Long filmId) {
        this.filmId = filmId;
    }

    public Integer getUseful() {
        return useful;
    }

    public void setUseful(final Integer useful) {
        this.useful = useful;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Review review = (Review) o;
        return Objects.equals(reviewId, review.reviewId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId);
    }
}

