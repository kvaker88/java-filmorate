package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.film.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({MpaRepository.class, MpaService.class, MpaController.class, MpaRowMapper.class})
class MpaServiceTest {

    @Autowired
    private MpaService mpaService;

    @Test
    @DisplayName("Получение всех MPA рейтингов → возвращает непустой список")
    void getAllMpa_shouldReturnNonEmptyList() {
        List<Mpa> mpaList = mpaService.getAllMpa();

        assertNotNull(mpaList);
        assertFalse(mpaList.isEmpty());
        assertTrue(mpaList.size() >= 1);
    }

    @Test
    @DisplayName("Получение MPA по несуществующему ID → исключение NotFoundException")
    void getMpaById_withNonExistingId_shouldThrowNotFoundException() {
        Long nonExistingId = 9999L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> mpaService.getMpaById(nonExistingId)
        );

        assertEquals("Рейтинг MPA с ID " + nonExistingId + " не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Получение MPA по ID = null → исключение NotFoundException")
    void getMpaById_withNullId_shouldThrowNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> mpaService.getMpaById(null)
        );

        assertTrue(exception.getMessage().contains("не найден"));
    }

    @Test
    @DisplayName("Получение MPA по отрицательному ID → исключение NotFoundException")
    void getMpaById_withNegativeId_shouldThrowNotFoundException() {
        Long negativeId = -1L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> mpaService.getMpaById(negativeId)
        );

        assertEquals("Рейтинг MPA с ID " + negativeId + " не найден", exception.getMessage());
    }
}