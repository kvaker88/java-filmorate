package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new ConcurrentHashMap<>();

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Попытка создания пользователя: {}", user);

        UserValidator.validate(user);
        UserValidator.validateName(user);

        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("Пользователь успешно создан. ID: {}, Логин: {}", user.getId(), user.getLogin());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        log.info("Попытка обновления пользователя: {}", newUser);

        if (newUser.getId() == null) {
            throw new ValidationException("ID должен быть указан");
        }

        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            throw new NotFoundException(String.format("Пользователь с ID = %d не найден", newUser.getId()));
        }

        UserValidator.validate(oldUser);
        oldUser.setEmail(newUser.getEmail().trim());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setBirthday(newUser.getBirthday());
        oldUser.setName(newUser.getName());
        UserValidator.validateName(oldUser);

        log.info("Пользователь с ID: {} успешно обновлен", newUser.getId());
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}