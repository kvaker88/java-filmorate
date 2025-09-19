package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.film.GenreRepository;
import ru.yandex.practicum.filmorate.repository.film.MpaRepository;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
public final class FilmValidator {
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static MpaRepository mpaRepository;
    private static GenreRepository genreRepository;

    private FilmValidator() {
    }

    public static void initRepositories(MpaRepository mpaRepo, GenreRepository genreRepo) {
        mpaRepository = mpaRepo;
        genreRepository = genreRepo;
    }

    public static void validate(Film film) {
        log.info("Начинается валидация фильма");

        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
        validateMpa(film);
        validateGenres(film);

        log.info("Валидация фильма прошла успешно");
    }

    private static void validateName(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка при валидации фильма: Название фильма не может быть пустым: {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }

    private static void validateDescription(Film film) {
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Ошибка при валидации фильма: Описание превышает лимит символов: {}",
                    film.getDescription().length());
            throw new ValidationException("Описание не может превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        }
    }

    private static void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            log.warn("Ошибка при валидации фильма: Дата релиза должна быть указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.warn("Ошибка при валидации фильма: Дата релиза не может быть раньше {}: {}",
                    FIRST_FILM_DATE,
                    film.getReleaseDate()
            );
            throw new ValidationException("Дата релиза не может быть раньше " + FIRST_FILM_DATE);
        }
    }

    private static void validateDuration(Film film) {
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Ошибка при валидации фильма: Продолжительность должна быть положительным числом: {}",
                    film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
    }

    private static void validateMpa(Film film) {
        Mpa mpa = film.getMpa();

        if (mpa == null) {
            return;
        }

        if (film.getMpa().getId() == null) {
            throw new ValidationException("ID MPA рейтинга должен быть указан");
        }

        if (mpa.getId() <= 0) {
            log.warn("Ошибка при валидации фильма: ID рейтинга MPA должен быть положительным числом: {}", mpa.getId());
            throw new ValidationException("ID рейтинга MPA должен быть положительным числом");
        }

        if (mpaRepository != null && !mpaRepository.existsById(mpa.getId())) {
            log.warn("Ошибка при валидации фильма: MPA рейтинг с ID {} не найден в базе данных", mpa.getId());
            throw new ValidationException("MPA рейтинг с ID " + mpa.getId() + " не найден");
        }

        if (mpa.getName() != null && mpa.getName().isBlank()) {
            log.warn("Ошибка при валидации фильма: Название рейтинга MPA не может быть пустым");
            throw new ValidationException("Название рейтинга MPA не может быть пустым");
        }
    }

    private static void validateGenres(Film film) {
        Set<Genre> genres = film.getGenres();

        if (genres == null || genres.isEmpty()) {
            return;
        }

        for (Genre genre : genres) {
            validateGenre(genre);
        }

        long distinctCount = genres.stream()
                .map(Genre::getId)
                .distinct()
                .count();

        if (distinctCount != genres.size()) {
            log.warn("Ошибка при валидации фильма: Найдены дубликаты жанров");
            throw new ValidationException("Фильм не может содержать дубликаты жанров");
        }
    }

    private static void validateGenre(Genre genre) {
        if (genre == null) {
            log.warn("Ошибка при валидации фильма: Жанр не может быть null");
            throw new ValidationException("Жанр не может быть null");
        }

        if (genre.getId() == null) {
            log.warn("Ошибка при валидации фильма: ID жанра должен быть указан");
            throw new ValidationException("ID жанра должен быть указан");
        }

        if (genre.getId() <= 0) {
            log.warn("Ошибка при валидации фильма: ID жанра должен быть положительным числом: {}", genre.getId());
            throw new ValidationException("ID жанра должен быть положительным числом");
        }

        if (genreRepository != null && !genreRepository.existsById(genre.getId())) {
            log.warn("Ошибка при валидации фильма: Жанр с ID {} не найден в базе данных", genre.getId());
            throw new ValidationException("Жанр с ID " + genre.getId() + " не найден");
        }

        if (genre.getName() != null && genre.getName().isBlank()) {
            log.warn("Ошибка при валидации фильма: Название жанра не может быть пустым");
            throw new ValidationException("Название жанра не может быть пустым");
        }
    }

    public static void validateForUpdate(Film film) {
        if (film.getId() == null) {
            log.warn("Ошибка при обновлении фильма: ID должен быть указан");
            throw new ValidationException("ID фильма должен быть указан при обновлении");
        }

        validate(film);
    }
}