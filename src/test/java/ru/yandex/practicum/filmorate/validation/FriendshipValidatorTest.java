package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.DirectorController;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.InvalidFriendshipException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.repository.film.DirectorRepository;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;
import ru.yandex.practicum.filmorate.repository.film.GenreRepository;
import ru.yandex.practicum.filmorate.repository.film.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mapper.*;
import ru.yandex.practicum.filmorate.repository.user.UserRepository;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({FilmRepository.class, UserRepository.class, GenreRepository.class, MpaRepository.class,
        FilmService.class, UserService.class, FilmController.class, UserController.class,
        FilmRowMapper.class, UserRowMapper.class, MpaRowMapper.class, GenreRowMapper.class,
        DirectorRepository.class, DirectorController.class, DirectorService.class, DirectorRowMapper.class})
class FriendshipValidatorTest {

    @Autowired
    private UserStorage userStorage;

    private User validUser1;
    private User validUser2;

    @BeforeEach
    void setUp() {
        validUser1 = new User(
                1L,
                "mail1@yandex.ru",
                "ПользовательОдин",
                "ИмяОдин",
                LocalDate.of(1990, 1, 1)
        );

        validUser2 = new User(
                2L,
                "mail2@yandex.ru",
                "ПользовательДва",
                "ИмяДва",
                LocalDate.of(1995, 5, 5)
        );

        userStorage.addUser(validUser1);
        userStorage.addUser(validUser2);
    }

    @Test
    @DisplayName("Успешная валидация дружбы → не вызывает исключения")
    void validate_WithValidUsers_ShouldNotThrowException() {
        assertDoesNotThrow(() ->
                FriendshipValidator.validate(
                        validUser1.getId(),
                        validUser2.getId(),
                        userStorage
                )
        );
    }

    @Test
    @DisplayName("Валидация с несуществующим пользователем (userId) → исключение NotFoundException")
    void validate_WithNonExistingUser_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> FriendshipValidator.validate(999L, validUser2.getId(), userStorage)
        );

        assertEquals("Пользователь с ID 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация с несуществующим другом (friendId) → исключение NotFoundException")
    void validate_WithNonExistingFriend_ShouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> FriendshipValidator.validate(validUser1.getId(), 999L, userStorage)
        );

        assertEquals("Друг с ID 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Валидация при попытке добавить самого себя в друзья → исключение InvalidFriendshipException")
    void validate_WithSameUserAndFriend_ShouldThrowInvalidFriendshipException() {
        InvalidFriendshipException exception = assertThrows(
                InvalidFriendshipException.class,
                () -> FriendshipValidator.validate(
                        validUser1.getId(),
                        validUser1.getId(),
                        userStorage
                )
        );

        assertEquals("Пользователь не может добавить сам себя в друзья", exception.getMessage());
    }
}
