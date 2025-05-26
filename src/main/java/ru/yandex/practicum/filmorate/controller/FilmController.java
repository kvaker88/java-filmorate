package ru.yandex.practicum.filmorate.controller;

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
    public Film createFilm(@RequestBody Film film) {
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

        if (!films.containsKey(film.getId())) {
            log.error((String.format("Фильм с ID = %d не найден", film.getId())));
            throw new NotFoundException(String.format("Фильм с ID = %d не найден", film.getId()));
        }
        FilmValidator.validate(film);
        films.put(film.getId(), film);
        log.info("Фильм с ID: {} успешно обновлен", film.getId());
        return film;
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