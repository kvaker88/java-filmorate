package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.dto.FriendshipResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.FriendshipValidator;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей. Текущее количество: {}", userStorage.getUsersSize());
        return userStorage.getAllUsers();
    }

    public User createUser(User user) {
        log.info("Запрос создания пользователя: {}", user);

        UserValidator.validate(user);
        UserValidator.validateName(user);

        user.setId(userStorage.getNextId());
        userStorage.addUser(user);

        log.info("Пользователь успешно создан. ID: {}, Логин: {}", user.getId(), user.getLogin());
        return user;
    }

    public User updateUser(@RequestBody User user) {
        log.info("Запрос обновления пользователя по данным: {}", user);

        if (user.getId() == null) {
            log.warn("Ошибка при обновлении пользователя: ID не указан");
            throw new ValidationException("ID пользователя должен быть указан при обновлении");
        }

        if (userStorage.doesUserNotExist(user.getId())) {
            log.warn((String.format("Фильм с ID %d не найден", user.getId())));
            throw new NotFoundException(String.format("Фильм с ID %d не найден", user.getId()));
        }

        UserValidator.validate(user);
        userStorage.addUser(user);
        log.info("Фильм с ID: {} успешно обновлен", user.getId());
        return user;
    }

    public User getUserById(Long userId) {
        log.info("Запрос получения пользователя с ID = {}", userId);

        if (userStorage.doesUserNotExist(userId)) {
            throw  new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }
        return userStorage.getUserById(userId);
    }

    public FriendshipResponse addFriend(Long userId, Long friendId) {
        FriendshipValidator.validate(userId, friendId, userStorage);

        userStorage.getUserById(userId).addFriend(friendId);
        userStorage.getUserById(friendId).addFriend(userId);

        return new FriendshipResponse(userId, friendId, "ADDED");
    }

    public FriendshipResponse deleteFriend(Long userId, Long friendId) {
        FriendshipValidator.validate(userId, friendId, userStorage);

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.deleteFriend(friendId);
        friend.deleteFriend(userId);

        log.info("Дружба между {} и {} успешно удалена", userId, friendId);
        return new FriendshipResponse(userId, friendId, "DELETED");
    }

    public List<User> getFriendsByUserId(Long userId) {
        log.info("Запрос получения друзей по ID пользователя = {}", userId);

        if (userStorage.doesUserNotExist(userId)) {
            throw  new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }

        Collection<Long> friendIds = userStorage.getUserById(userId).getFriends();

        return friendIds.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherId);

        FriendshipValidator.validate(userId, otherId, userStorage);

        Collection<Long> userFriends = new HashSet<>(
                user.getFriends() != null ?
                        user.getFriends() : Set.of());

        Collection<Long> otherUserFriends = new HashSet<>(
                otherUser.getFriends() != null ?
                        otherUser.getFriends() : Set.of());

        if (userFriends.isEmpty() || otherUserFriends.isEmpty()) {
            return Set.of(); // Ранний вывод, если один из листов пуст
        }

        Set<Long> commonFriends = new HashSet<>(userFriends); // Для сохранения иммутабельности
        commonFriends.retainAll(otherUserFriends);

        return commonFriends.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}


