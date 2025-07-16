package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest {
    @Autowired
    private UserController userController;
    private User validUser1;
    private User validUser2;

    @BeforeEach
    void setUp() {
        // Создаем валидных пользователей
        validUser1 = new User(
                null,
                "user1@yandex.ru",
                "ЛогинОдин",
                "ИмяОдин",
                LocalDate.of(1990, 1, 1)
        );

        validUser2 = new User(
                null,
                "user2@yandex.ru",
                "ЛогинДва",
                "ИмяДва",
                LocalDate.of(1995, 5, 5)
        );

        validUser1 = userController.createUser(validUser1);
        validUser2 = userController.createUser(validUser2);
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными → возвращает пользователя с ID")
    void createUser_withValidData_shouldReturnUserWithId() {
        User newUser = new User(
                null,
                "newUser@yandex.ru",
                "НовыйЛогин",
                "НовоеИмя",
                LocalDate.of(2000, 1, 1)
        );

        User createdUser = userController.createUser(newUser);

        assertNotNull(createdUser.getId());
        assertEquals("НовыйЛогин", createdUser.getLogin());
    }

    @Test
    @DisplayName("Создание пользователя с email без @ → исключение ValidationException")
    void createUser_withInvalidEmail_shouldThrowValidationException() {
        User invalidUser = new User(
                null,
                "Некорректный-Емейл",
                "НекорректныйПользователь",
                "НекорректныйПользователь",
                LocalDate.of(2000, 1, 1)
        );

        assertThrows(ValidationException.class, () -> userController.createUser(invalidUser));
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ID → исключение NotFoundException")
    void getUserById_withNonExistingId_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> userController.getUserById(9999L));
    }

    @Test
    @DisplayName("Получение всех пользователей → возвращает список всех созданных пользователей")
    void getAllUsers_shouldReturnAllCreatedUsers() {
        Collection<User> users = userController.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getLogin().equals("ЛогинОдин")));
        assertTrue(users.stream().anyMatch(u -> u.getLogin().equals("ЛогинДва")));
    }

    @Test
    @DisplayName("Обновление пользователя с валидными данными → успешное обновление")
    void updateUser_withValidData_shouldUpdateUser() {
        validUser1.setName("ОбновлённоеИмя");

        User updatedUser = userController.updateUser(validUser1);

        assertEquals("ОбновлённоеИмя", updatedUser.getName());
        assertEquals(validUser1.getId(), updatedUser.getId());
    }

    @Test
    @DisplayName("Добавление друга с валидными ID → возвращает ответ о дружбе")
    void addFriend_withValidIds_shouldReturnFriendshipResponse() {
        User response = userController.addFriend(validUser1.getId(), validUser2.getId()).getBody();

        assertNotNull(response);
        assertEquals(validUser1.getId(), response.getId());
    }

    @Test
    @DisplayName("Получение списка друзей пользователя → возвращает список друзей")
    void getFriendsByUserId_shouldReturnFriendsList() {
        userController.addFriend(validUser1.getId(), validUser2.getId());

        Collection<User> friends = userController.getFriendsByUserId(validUser1.getId());

        assertEquals(1, friends.size());
        assertEquals(validUser2.getId(), friends.iterator().next().getId());
    }

    @Test
    @DisplayName("Удаление друга → успешно удаляет дружбу")
    void deleteFriend_shouldRemoveFriendship() {
        userController.addFriend(validUser1.getId(), validUser2.getId());

        User response = userController.deleteFriend(validUser1.getId(), validUser2.getId()).getBody();

        assertNotNull(response);
        Collection<User> friends = userController.getFriendsByUserId(validUser1.getId());
        assertTrue(friends.isEmpty());
    }
}