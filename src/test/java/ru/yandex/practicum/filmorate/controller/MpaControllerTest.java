package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.film.MpaRepository;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({MpaRepository.class, MpaService.class, MpaController.class, MpaRowMapper.class})
class MpaControllerTest {

    @Autowired
    private MpaController mpaController;

    @Test
    @DisplayName("Получение всех MPA рейтингов → возвращает непустой список")
    void getAllMpa_shouldReturnNonEmptyCollection() {
        Collection<Mpa> mpaList = mpaController.getAllMpa();

        assertNotNull(mpaList);
        assertFalse(mpaList.isEmpty());
    }

    @Test
    @DisplayName("Получение MPA по существующему ID → возвращает корректный MPA")
    void getMpaById_withExistingId_shouldReturnMpa() {
        Mpa mpa = mpaController.getMpaById(1L);

        assertNotNull(mpa);
        assertEquals(1L, mpa.getId());
        assertNotNull(mpa.getName());
    }

    @Test
    @DisplayName("Получение MPA по несуществующему ID → исключение NotFoundException")
    void getMpaById_withNonExistingId_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> mpaController.getMpaById(9999L));
    }
}