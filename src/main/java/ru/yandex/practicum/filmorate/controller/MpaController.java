package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public List<Mpa> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");
        List<Mpa> mpaList = mpaService.getAllMpa();
        log.info("Возвращено {} рейтингов MPA", mpaList.size());
        return mpaList;
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable Long id) {
        log.info("Запрос на получение рейтинга MPA с ID: {}", id);
        Mpa mpa = mpaService.getMpaById(id);
        log.info("Найден рейтинг MPA: {}", mpa.getName());
        return mpa;
    }
}