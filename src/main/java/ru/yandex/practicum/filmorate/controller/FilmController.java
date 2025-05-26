package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new ConcurrentHashMap<>();

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Попытка создания фильма: {}", film);

        FilmValidator.validate(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм успешно создан. ID: {}, Название: {}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Попытка обновления фильма: {}", film);

        if (film.getId() == null) {
            log.error("Ошибка при обновлении фильма: ID должен быть указан");
            throw new ValidationException("ID должен быть указан");
        }

        Film existingFilm = films.get(film.getId());
        if (existingFilm == null) {
            log.error((String.format("Фильм с ID = %d не найден", film.getId())));
            throw new NotFoundException(String.format("Фильм с ID = %d не найден", film.getId()));
        }

        FilmValidator.validate(film);

        if (film.getName() != null) {
            existingFilm.setName(film.getName());
            log.info("Имя фильма изменено: {}", film.getName());
        }
        if (film.getDescription() != null) {
            existingFilm.setDescription(film.getDescription());
            log.info("Описание фильма изменено: {}", film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            existingFilm.setReleaseDate(film.getReleaseDate());
            log.info("Дата релиза фильма изменено: {}", film.getReleaseDate());
        }
        if (film.getDuration() != null) {
            existingFilm.setDuration(film.getDuration());
            log.info("Продолжительность фильма изменена: {}", film.getDuration());
        }

        log.info("Фильм с ID: {} успешно обновлен", film.getId());
        return existingFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}