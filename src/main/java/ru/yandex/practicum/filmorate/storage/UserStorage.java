package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    Long getUsersSize();

    List<User> getAllUsers();

    User getUserById(Long userId);

    void addUser(User user);

    boolean doesUserNotExist(Long id);

    long getNextId();
}
