package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );
    }

    @Test
    void createUser_withValidData_shouldCreateUser() {
        User createdUser = userController.createUser(validUser);

        assertNotNull(createdUser.getId());
        assertEquals(1L, createdUser.getId());
        assertEquals("Логин", createdUser.getLogin());
    }

    @Test
    void createUser_withEmptyEmail_shouldThrowValidationException() {
        User user = new User(
                null,
                "",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );
        assertThrows(ValidationException.class,
                () -> userController.createUser(user));
    }

    @Test
    void createUser_withInvalidEmail_shouldThrowValidationException() {
        User user = new User(
                null,
                "Некорректный_Email",
                "Логин",
                "Имя",
                LocalDate.now()
        );

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_withEmptyLogin_shouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "",
                "Имя",
                LocalDate.now()
        );

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_withLoginContainingSpaces_shouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Лог ин",
                "Имя",
                LocalDate.now()
        );

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_withFutureBirthday_shouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.now().plusDays(1)
        );

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void createUser_withNullName_shouldUseLoginAsName() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                null,
                LocalDate.now()
        );
        User createdUser = userController.createUser(user);

        assertEquals("Логин", createdUser.getName());
    }

    @Test
    void updateUser_withNonExistingId_shouldThrowNotFoundException() {
        User user = new User(
                999L,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.now()
        );

        assertThrows(NotFoundException.class, () -> userController.updateUser(user));
    }

    @Test
    void updateUser_withValidData_shouldUpdateUser() {
        User createdUser = userController.createUser(validUser);
        User updateData = new User(
                createdUser.getId(),
                "newMail@yandex.ru",
                "Новый_Логин",
                "Новое_Имя",
                LocalDate.of(1995, 1, 1)
        );

        User updatedUser = userController.updateUser(updateData);

        assertEquals("newMail@yandex.ru", updatedUser.getEmail());
        assertEquals("Новый_Логин", updatedUser.getLogin());
        assertEquals("Новое_Имя", updatedUser.getName());
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        userController.createUser(validUser);
        User anotherUser = new User(
                null,
                "anotherMail@yandex.ru",
                "Другой_Логин",
                null,
                LocalDate.now()
        );
        userController.createUser(anotherUser);

        assertEquals(2, userController.getAllUsers().size());
    }
}