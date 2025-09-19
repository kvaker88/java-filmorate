package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long reviewId;

    @NotBlank(message = "Отзыв не может быть пустым")
    private String content;

    @NotNull(message = "Поле isPositive обязательно")
    private Boolean isPositive;

    @NotNull(message = "Не указан пользователь")
    private Long userId;

    @NotNull(message = "Не указан фильм")
    private Long filmId;

    private Integer useful;
}


