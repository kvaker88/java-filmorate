package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.HashSet;
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

    HashSet<Long> getFriendsById(Long userId);

    List<Long> getFriendIds(Long userId);
}
