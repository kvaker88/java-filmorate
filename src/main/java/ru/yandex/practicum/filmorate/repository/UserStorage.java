package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAllUsers();

    User getUserById(Long userId);

    void addUser(User user);

    void updateUser(User user);

    List<User> getFriends(Long userId);

    boolean doesUserNotExist(Long id);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<Long> getFriendIds(Long userId);

    List<User> getFriendsByUserId(Long userId);

    List<User> getCommonFriends(Long userId, Long otherId);

    boolean existsById(long id);

    void deleteById(long id);
}
