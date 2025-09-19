package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    private Long reviewId;

    @NotBlank(message = "Review content must not be blank")
    private String content;

    @NotNull(message = "isPositive must be specified")
    @JsonProperty("isPositive")
    private Boolean isPositive;

    @NotNull(message = "userId must be specified")
    private Long userId;

    @NotNull(message = "filmId must be specified")
    private Long filmId;

    // рейтинг полезности; по умолчанию 0
    private Integer useful;
}


