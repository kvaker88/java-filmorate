package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Slf4j
public class LikeValidator {
    private LikeValidator() {
    }

    public static void validate(Long filmId, Long userId,
                                UserStorage userStorage,
                                FilmStorage filmStorage
    ) {
        log.info("Валидация лайка фильму = {}, от пользователя = {}", filmId, userId);

        if (userStorage.doesUserNotExist(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }

        if (filmStorage.doesFilmNotExist(filmId)) {
            log.warn("Фильм с ID {} не найден", filmId);
            throw new NotFoundException(String.format("Фильм с ID %d не найден", filmId));
        }
    }
}

