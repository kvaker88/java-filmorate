package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dto.FriendshipResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {
    @Autowired
    private UserService userService;
    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными → возвращает пользователя с ID")
    void createUser_withValidData_shouldCreateUser() {
        User createdUser = userService.createUser(validUser);

        assertNotNull(createdUser.getId());
        assertEquals(1L, createdUser.getId());
        assertEquals("Логин", createdUser.getLogin());
    }

    @Test
    @DisplayName("Создание пользователя с пустым email → исключение ValidationException")
    void createUser_withEmptyEmail_shouldThrowValidationException() {
        User user = new User(
                null,
                "",
                "Логин",
                "Имя",
                LocalDate.of(1990, 1, 1)
        );
        assertThrows(ValidationException.class,
                () -> userService.createUser(user));
    }

    @Test
    @DisplayName("Создание пользователя с email без @ → исключение ValidationException")
    void createUser_withInvalidEmail_shouldThrowValidationException() {
        User user = new User(
                null,
                "Некорректный_Email",
                "Логин",
                "Имя",
                LocalDate.now()
        );

        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    @DisplayName("Создание пользователя с пустым логином → исключение ValidationException")
    void createUser_withEmptyLogin_shouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "",
                "Имя",
                LocalDate.now()
        );

        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    @DisplayName("Создание пользователя с логином, содержащим пробелы → исключение ValidationException")
    void createUser_withLoginContainingSpaces_shouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Лог ин",
                "Имя",
                LocalDate.now()
        );

        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    @DisplayName("Создание пользователя с датой рождения в будущем → исключение ValidationException")
    void createUser_withFutureBirthday_shouldThrowValidationException() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.now().plusDays(1)
        );

        assertThrows(ValidationException.class, () -> userService.createUser(user));
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем → подставляет логин в качестве имени")
    void createUser_withNullName_shouldUseLoginAsName() {
        User user = new User(
                null,
                "mail@yandex.ru",
                "Логин",
                null,
                LocalDate.now()
        );
        User createdUser = userService.createUser(user);

        assertEquals("Логин", createdUser.getName());
    }

    @Test
    @DisplayName("Обновление пользователя с несуществующим ID → исключение NotFoundException")
    void updateUser_withNonExistingId_shouldThrowNotFoundException() {
        User user = new User(
                999L,
                "mail@yandex.ru",
                "Логин",
                "Имя",
                LocalDate.now()
        );

        assertThrows(NotFoundException.class, () -> userService.updateUser(user));
    }

    @Test
    @DisplayName("Обновление пользователя с валидными данными → успешное обновление")
    void updateUser_withValidData_shouldUpdateUser() {
        User createdUser = userService.createUser(validUser);
        User updateData = new User(
                createdUser.getId(),
                "newMail@yandex.ru",
                "Новый_Логин",
                "Новое_Имя",
                LocalDate.of(1995, 1, 1)
        );

        User updatedUser = userService.updateUser(updateData);

        assertEquals("newMail@yandex.ru", updatedUser.getEmail());
        assertEquals("Новый_Логин", updatedUser.getLogin());
        assertEquals("Новое_Имя", updatedUser.getName());
    }

    @Test
    @DisplayName("Получение всех пользователей → возвращает список всех созданных пользователей")
    void getAllUsers_shouldReturnAllUsers() {
        userService.createUser(validUser);
        User anotherUser = new User(
                null,
                "anotherMail@yandex.ru",
                "Другой_Логин",
                null,
                LocalDate.now()
        );
        userService.createUser(anotherUser);

        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    @DisplayName("Добавление друга с несуществующим ID пользователя → исключение NotFoundException")
    void addFriend_withNonExistingUser_shouldThrowNotFoundException() {
        User user1 = userService.createUser(validUser);
        Long nonExistingUserId = 999L;

        assertThrows(NotFoundException.class,
                () -> userService.addFriend(user1.getId(), nonExistingUserId));
    }

    @Test
    @DisplayName("Добавление друга с валидными ID → успешное добавление")
    void addFriend_withValidUsers_shouldAddMutualFriendship() {
        User user1 = userService.createUser(validUser);
        User user2 = userService.createUser(new User(
                null,
                "friend@yandex.ru",
                "ЛогинДруга",
                "ИмяДруга",
                LocalDate.now()
        ));

        FriendshipResponse response = userService.addFriend(user1.getId(), user2.getId());

        assertEquals("ADDED", response.getStatus());
        assertTrue(userService.getFriendsByUserId(user1.getId()).contains(user2));
        assertTrue(userService.getFriendsByUserId(user2.getId()).contains(user1));
    }

    @Test
    @DisplayName("Удаление друга → успешное удаление дружбы")
    void deleteFriend_withExistingFriendship_shouldRemoveFriendship() {
        User user1 = userService.createUser(validUser);
        User user2 = userService.createUser(new User(
                null,
                "friend@yandex.ru",
                "ЛогинДруга",
                "ИмяДруга",
                LocalDate.now()
        ));

        userService.addFriend(user1.getId(), user2.getId());
        FriendshipResponse response = userService.deleteFriend(user1.getId(), user2.getId());

        assertEquals("DELETED", response.getStatus());
        assertFalse(userService.getFriendsByUserId(user1.getId()).contains(user2));
        assertFalse(userService.getFriendsByUserId(user2.getId()).contains(user1));
    }

    @Test
    @DisplayName("Получение списка друзей несуществующего пользователя → исключение NotFoundException")
    void getFriendsByUserId_withNonExistingUser_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> userService.getFriendsByUserId(999L));
    }

    @Test
    @DisplayName("Получение списка друзей пользователя без друзей → возвращает пустой список")
    void getFriendsByUserId_withNoFriends_shouldReturnEmptyList() {
        User user = userService.createUser(validUser);

        List<User> friends = userService.getFriendsByUserId(user.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    @DisplayName("Получение списка друзей → возвращает список друзей пользователя")
    void getFriendsByUserId_withFriends_shouldReturnFriendsList() {
        User user1 = userService.createUser(validUser);
        User user2 = userService.createUser(new User(
                null,
                "friend@yandex.ru",
                "ЛогинДруга",
                "ИмяДруга",
                LocalDate.now()
        ));

        User user3 = userService.createUser(new User(
                null,
                "anotherFriend@yandex.ru",
                "ЛогинДругогоДруга",
                "ИмяДругогоДруга",
                LocalDate.now()
        ));

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        List<User> friends = userService.getFriendsByUserId(user1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.containsAll(List.of(user2, user3)));
    }

    @Test
    @DisplayName("Получение общих друзей когда их нет → возвращает пустой список")
    void getCommonFriends_withNoCommonFriends_shouldReturnEmptyList() {
        User user1 = userService.createUser(validUser);
        User user2 = userService.createUser(new User(
                null,
                "friend@yandex.ru",
                "ЛогинДруга",
                "ИмяДруга",
                LocalDate.now()
        ));

        User user3 = userService.createUser(new User(
                null,
                "anotherFriend@yandex.ru",
                "ЛогинДругогоДруга",
                "ИмяДругогоДруга",
                LocalDate.now()
        ));

        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user2.getId(), user3.getId());

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(user3));
    }

    @Test
    @DisplayName("Получение общих друзей когда у одного пользователя нет друзей → возвращает пустой список")
    void getCommonFriends_whenOneUserHasNoFriends_shouldReturnEmptyList() {
        User user1 = userService.createUser(validUser);
        User user2 = userService.createUser(new User(
                null,
                "friend@yandex.ru",
                "ЛогинДруга",
                "ИмяДруга",
                LocalDate.now()
        ));

        User user3 = userService.createUser(new User(
                null,
                "anotherFriend@yandex.ru",
                "ЛогинДругогоДруга",
                "ИмяДругогоДруга",
                LocalDate.now()
        ));

        userService.addFriend(user1.getId(), user3.getId());

        Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.isEmpty());
    }
}