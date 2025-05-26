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
    public User updateUser(@RequestBody User user) {
        log.info("Попытка обновления пользователя: {}", user);

        if (user.getId() == null) {
            log.error("Ошибка при обновлении пользователя: ID должен быть указан");
            throw new ValidationException("ID должен быть указан");
        }

        if (!users.containsKey(user.getId())) {
            log.error((String.format("Фильм с ID = %d не найден", user.getId())));
            throw new NotFoundException(String.format("Фильм с ID = %d не найден", user.getId()));
        }
        UserValidator.validate(user);
        users.put(user.getId(), user);
        log.info("Фильм с ID: {} успешно обновлен", user.getId());
        return user;
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