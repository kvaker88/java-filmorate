package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.validation.FriendshipValidator;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    // УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ПО ID
    public void deleteById(long userId) {
        log.info("Запрос на удаление пользователя: {}", userId);
        if (userStorage.doesUserNotExist(userId)) {
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }
        userStorage.deleteById(userId);
        log.info("Пользователь {} удалён", userId);
    }

    public List<User> getAllUsers() {
        var users = userStorage.getAllUsers();
        log.info("Запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users;
    }

    public User createUser(User user) {
        log.info("Запрос создания пользователя: {}", user);
        UserValidator.validate(user);
        UserValidator.validateName(user);
        userStorage.addUser(user);
        log.info("Пользователь успешно создан. ID: {}, Логин: {}", user.getId(), user.getLogin());
        return user;
    }

    public User updateUser(User user) {
        log.info("Запрос обновления пользователя по данным: {}", user);

        if (user.getId() == null) {
            log.warn("Ошибка при обновлении пользователя: ID не указан");
            throw new ValidationException("ID пользователя должен быть указан при обновлении");
        }

        if (userStorage.doesUserNotExist(user.getId())) {
            log.warn(String.format("Пользователь с ID %d не найден", user.getId()));
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", user.getId()));
        }

        UserValidator.validate(user);
        userStorage.updateUser(user);
        log.info("Пользователь с ID: {} успешно обновлён", user.getId());
        return user;
    }

    public User getUserById(Long userId) {
        log.info("Запрос получения пользователя с ID = {}", userId);
        if (userStorage.doesUserNotExist(userId)) {
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }
        return userStorage.getUserById(userId);
    }

    public User addFriend(Long userId, Long friendId) {
        log.info("Запрос на добавление дружбы между {} и {}", userId, friendId);
        FriendshipValidator.validate(userId, friendId, userStorage);
        userStorage.addFriend(userId, friendId);
        var user = userStorage.getUserById(userId);
        log.info("Дружба между {} и {} успешно добавлена", userId, friendId);
        return user;
    }

    public User deleteFriend(Long userId, Long friendId) {
        log.info("Запрос на удаление дружбы между {} и {}", userId, friendId);
        FriendshipValidator.validate(userId, friendId, userStorage);
        userStorage.deleteFriend(userId, friendId);
        var user = userStorage.getUserById(userId);
        log.info("Дружба между {} и {} успешно удалена", userId, friendId);
        return user;
    }

    public List<User> getFriendsByUserId(Long userId) {
        log.info("Запрос получения друзей по ID пользователя = {}", userId);
        if (userStorage.doesUserNotExist(userId)) {
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }
        return userStorage.getFriendsByUserId(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Запрос общих друзей пользователей {} и {}", userId, otherId);
        FriendshipValidator.validate(userId, otherId, userStorage);
        return userStorage.getCommonFriends(userId, otherId);
    }
}
