package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/{userId}/friends")
    public Collection<User> getFriendsByUserId(@PathVariable Long userId) {
        return userService.getFriendsByUserId(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(
            @PathVariable Long userId,
            @PathVariable Long otherId
    ) {
        return userService.getCommonFriends(userId, otherId);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<User> addFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId
    ) {
        User response = userService.addFriend(userId, friendId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long userId) {
        log.info("GET /users/{}/recommendations", userId);
        return userService.getRecommendations(userId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<User> deleteFriend(
            @PathVariable Long userId,
            @PathVariable Long friendId
    ) {
        User response = userService.deleteFriend(userId, friendId);
        return ResponseEntity.ok(response);
    }
}
