package ru.yandex.practicum.filmorate.repository.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.film.GenreRepository;
import ru.yandex.practicum.filmorate.repository.film.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmRepository.class, UserRepository.class, GenreRepository.class, MpaRepository.class,
        FilmService.class, UserService.class, FilmController.class, UserController.class,
        FilmRowMapper.class, UserRowMapper.class, MpaRowMapper.class, GenreRowMapper.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User validUser1;
    private User validUser2;
    private User validUser3;

    @BeforeEach
    void setUp() {
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

        validUser3 = new User(
                null,
                "user3@yandex.ru",
                "ЛогинТри",
                "ИмяТри",
                LocalDate.of(1999, 8, 10)
        );

        userRepository.addUser(validUser1);
        userRepository.addUser(validUser2);
        userRepository.addUser(validUser3);
    }

    @Test
    @DisplayName("Получение количества пользователей → возвращает корректное число")
    void getUsersSize_shouldReturnCorrectCount() {
        Long size = userRepository.getUsersSize();
        assertEquals(3, size);
    }

    @Test
    @DisplayName("Получение всех пользователей → возвращает непустую коллекцию")
    void getAllUsers_shouldReturnNonEmptyCollection() {
        List<User> users = userRepository.getAllUsers();

        assertNotNull(users);
        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ID → исключение NotFoundException")
    void getUserById_withNonExistingId_shouldThrowNotFoundException() {
        Long nonExistingId = 9999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userRepository.getUserById(nonExistingId)
        );

        assertTrue(exception.getMessage().contains("Пользователь с ID " + nonExistingId + " не найден"));
    }

    @Test
    @DisplayName("Добавление пользователя с валидными данными → успешно сохраняет пользователя")
    void addUser_withValidData_shouldSaveUser() {
        User newUser = new User(
                null,
                "new@yandex.ru",
                "Новый",
                "New User",
                LocalDate.of(1995, 1, 1)
        );

        userRepository.addUser(newUser);

        assertNotNull(newUser.getId());
        User savedUser = userRepository.getUserById(newUser.getId());
        assertEquals("new@yandex.ru", savedUser.getEmail());
        assertEquals("Новый", savedUser.getLogin());
    }

    @Test
    @DisplayName("Обновление пользователя → успешно обновляет данные")
    void updateUser_shouldUpdateUserData() {
        validUser1.setName("Обновлённый");
        validUser1.setEmail("updated@yandex.ru");

        userRepository.updateUser(validUser1);

        User updatedUser = userRepository.getUserById(validUser1.getId());
        assertEquals("Обновлённый", updatedUser.getName());
        assertEquals("updated@yandex.ru", updatedUser.getEmail());
    }

    @Test
    @DisplayName("Добавление друга → успешно добавляет друга")
    void addFriend_shouldAddFriend() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());

        User user1 = userRepository.getUserById(validUser1.getId());
        assertTrue(user1.getFriends().contains(validUser2.getId()));
    }

    @Test
    @DisplayName("Добавление самого себя в друзья → исключение ValidationException")
    void addFriend_withSelf_shouldThrowValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userRepository.addFriend(validUser1.getId(), validUser1.getId())
        );

        assertEquals("Нельзя добавить самого себя в друзья", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление дублирующей дружбы → не создает дубликат")
    void addFriend_duplicateFriendship_shouldNotCreateDuplicate() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());
        userRepository.addFriend(validUser1.getId(), validUser2.getId());

        User user1 = userRepository.getUserById(validUser1.getId());
        assertEquals(1, user1.getFriends().size());
    }

    @Test
    @DisplayName("Взаимное добавление в друзья → создает взаимную дружбу")
    void addFriend_mutualFriendship_shouldCreateMutualFriendship() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());
        userRepository.addFriend(validUser2.getId(), validUser1.getId());

        User user1 = userRepository.getUserById(validUser1.getId());
        User user2 = userRepository.getUserById(validUser2.getId());

        assertTrue(user1.getFriends().contains(validUser2.getId()));
        assertTrue(user2.getFriends().contains(validUser1.getId()));
    }

    @Test
    @DisplayName("Удаление друга → успешно удаляет друга")
    void deleteFriend_shouldRemoveFriend() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());

        userRepository.deleteFriend(validUser1.getId(), validUser2.getId());

        User user1 = userRepository.getUserById(validUser1.getId());
        assertFalse(user1.getFriends().contains(validUser2.getId()));
    }

    @Test
    @DisplayName("Удаление несуществующего друга → не вызывает ошибок")
    void deleteFriend_nonExistingFriend_shouldNotThrowError() {
        assertDoesNotThrow(() -> userRepository.deleteFriend(validUser1.getId(), validUser2.getId()));
    }

    @Test
    @DisplayName("Получение списка друзей → возвращает список друзей")
    void getFriends_shouldReturnFriendsList() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());
        userRepository.addFriend(validUser1.getId(), validUser3.getId());

        List<User> friends = userRepository.getFriends(validUser1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(validUser2.getId())));
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(validUser3.getId())));
    }

    @Test
    @DisplayName("Получение списка друзей для пользователя без друзей → возвращает пустой список")
    void getFriends_withNoFriends_shouldReturnEmptyList() {
        List<User> friends = userRepository.getFriends(validUser1.getId());

        assertNotNull(friends);
        assertTrue(friends.isEmpty());
    }

    @Test
    @DisplayName("Проверка существования пользователя → возвращает false для существующего пользователя")
    void doesUserNotExist_withExistingUser_shouldReturnFalse() {
        boolean result = userRepository.doesUserNotExist(validUser1.getId());
        assertFalse(result);
    }

    @Test
    @DisplayName("Проверка существования пользователя → возвращает true для несуществующего пользователя")
    void doesUserNotExist_withNonExistingUser_shouldReturnTrue() {
        boolean result = userRepository.doesUserNotExist(9999L);
        assertTrue(result);
    }

    @Test
    @DisplayName("Получение друзей по ID → возвращает множество ID друзей")
    void getFriendsById_shouldReturnFriendsSet() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());
        userRepository.addFriend(validUser1.getId(), validUser3.getId());

        HashSet<Long> friends = userRepository.getFriendsById(validUser1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.contains(validUser2.getId()));
        assertTrue(friends.contains(validUser3.getId()));
    }

    @Test
    @DisplayName("Получение друзей по ID для пользователя без друзей → возвращает пустое множество")
    void getFriendsById_withNoFriends_shouldReturnEmptySet() {
        HashSet<Long> friends = userRepository.getFriendsById(validUser1.getId());

        assertNotNull(friends);
        assertTrue(friends.isEmpty());
    }

    @Test
    @DisplayName("Загрузка отношений пользователя → корректно загружает друзей и заявки")
    void loadUserRelations_shouldLoadRelationsCorrectly() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());
        userRepository.addFriend(validUser3.getId(), validUser1.getId());

        User user1 = userRepository.getUserById(validUser1.getId());

        assertTrue(user1.getFriends().contains(validUser2.getId()));

        assertTrue(user1.getFriendIncomingRequests().contains(validUser3.getId()));
    }

    @Test
    @DisplayName("Взаимная дружба → оба пользователя видят друг друга в друзьях")
    void mutualFriendship_bothUsersShouldSeeEachOther() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());
        userRepository.addFriend(validUser2.getId(), validUser1.getId());

        User user1 = userRepository.getUserById(validUser1.getId());
        User user2 = userRepository.getUserById(validUser2.getId());

        assertTrue(user1.getFriends().contains(validUser2.getId()));
        assertTrue(user2.getFriends().contains(validUser1.getId()));
    }

    @Test
    @DisplayName("Отправка заявки в друзья → создает заявку")
    void friendRequest_shouldCreatePendingRequest() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());

        User user2 = userRepository.getUserById(validUser2.getId());
        assertTrue(user2.getFriendIncomingRequests().contains(validUser1.getId()));
    }

    @Test
    @DisplayName("Принятие заявки в друзья → создает взаимную дружбу")
    void acceptFriendRequest_shouldCreateMutualFriendship() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());

        userRepository.addFriend(validUser2.getId(), validUser1.getId());

        User user1 = userRepository.getUserById(validUser1.getId());
        User user2 = userRepository.getUserById(validUser2.getId());

        assertTrue(user1.getFriends().contains(validUser2.getId()));
        assertTrue(user2.getFriends().contains(validUser1.getId()));

        assertFalse(user2.getFriendIncomingRequests().contains(validUser1.getId()));
    }

    @Test
    @DisplayName("Обновление пользователя не затрагивает отношения")
    void updateUser_shouldNotAffectRelations() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());

        validUser1.setName("Updated Name");
        userRepository.updateUser(validUser1);

        User updatedUser = userRepository.getUserById(validUser1.getId());
        assertTrue(updatedUser.getFriends().contains(validUser2.getId()));
        assertEquals("Updated Name", updatedUser.getName());
    }

    @Test
    @DisplayName("Получение пользователя с полными данными отношений")
    void getUserById_shouldReturnUserWithFullRelations() {
        userRepository.addFriend(validUser1.getId(), validUser2.getId());
        userRepository.addFriend(validUser3.getId(), validUser1.getId());

        User user = userRepository.getUserById(validUser1.getId());

        assertNotNull(user.getFriends());
        assertNotNull(user.getFriendIncomingRequests());
        assertTrue(user.getFriends().contains(validUser2.getId()));
        assertTrue(user.getFriendIncomingRequests().contains(validUser3.getId()));
    }
}