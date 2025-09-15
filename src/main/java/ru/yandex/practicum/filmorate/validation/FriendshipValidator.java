package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.InvalidFriendshipException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.repository.UserStorage;

@Slf4j
public class FriendshipValidator {
    private FriendshipValidator() {
    }

    public static void validate(Long userId, Long friendId,
                                UserStorage userStorage
    ) {
        log.info("Начинается валидация дружбы");

        if (userStorage.doesUserNotExist(userId)) {
            log.warn("Ошибка поиска пользователя, который хотел добавить в друзья. ID пользователя = {}",
                    userId);
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }

        if (userStorage.doesUserNotExist(friendId)) {
            log.warn("Ошибка поиска пользователя, которого хотели добавить в друзья. ID друга = {}",
                    friendId);
            throw new NotFoundException(String.format("Друг с ID %d не найден", friendId));
        }

        if (userId.equals(friendId)) {
            log.warn("Ошибка добавления в друзья. Пользователь пытался добавить сам себя. ID пользователя = {}",
                    userId);
            throw new InvalidFriendshipException("Пользователь не может добавить сам себя в друзья");
        }
        log.info("Валидация дружбы завершена.");
    }
}
