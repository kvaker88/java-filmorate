package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    //GET /films/director/{directorId}?sortBy=[year,likes]
    //Возвращает список фильмов режиссера отсортированных по количеству лайков или году выпуска.

    //GET /directors - Список всех режиссёров
    @GetMapping
    public Collection<Director> getAll() {
        return directorService.getAll();
    }

    // GET /directorsç{id}- Получение режиссёра по id
    @GetMapping("/{id}")
    public Director getById(@PathVariable("id") Long directorId) {
        return directorService.getById(directorId);
    }

    // POST /directors - Создание режиссёра
    @PostMapping
    public Director createDirector(@RequestBody Director director) {
        return directorService.createDirector(director);
    }

    // PUT /directors - Изменение режиссёра
    @PutMapping
    public Director updateFilm(@RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    // DELETE /directors/{id} - Удаление режиссёра
    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable("id") Long directorId) {
        directorService.removeDirectorById(directorId);
    }
}
