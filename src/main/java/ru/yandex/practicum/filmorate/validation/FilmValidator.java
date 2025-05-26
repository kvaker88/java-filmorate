package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;

@Slf4j
public final class FilmValidator {
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private FilmValidator() {

    }

    public static void validate(Film film) {
        log.info("Начинается валидация фильма");

        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка при валидации фильма: Название фильма не может быть пустым: {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.error("Ошибка при валидации фильма: Описание превышает лимит символов: {}",
                    film.getDescription().length());
            throw new ValidationException("Описание не может превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        if (film.getReleaseDate() == null) {
            log.error("Ошибка при валидации фильма: Дата релиза должна быть указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.error("Ошибка при валидации фильма: Дата релиза не может быть раньше {}: {}",
                    FIRST_FILM_DATE,
                    film.getReleaseDate()
            );
            throw new ValidationException("Дата релиза не может быть раньше " + FIRST_FILM_DATE);
        }

        if (film.getDuration() == null || film.getDuration().isNegative() || film.getDuration().isZero()) {
            log.error("Ошибка при валидации фильма: Продолжительность должна быть положительным числом: {}",
                    film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }

        log.info("Валидация фильма прошла успешно");
    }
}