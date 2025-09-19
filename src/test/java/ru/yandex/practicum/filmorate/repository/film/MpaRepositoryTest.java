package ru.yandex.practicum.filmorate.repository.film;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({MpaRepository.class, MpaService.class, MpaController.class, MpaRowMapper.class})
class MpaRepositoryTest {

    @Autowired
    private MpaRepository mpaRepository;

    @Test
    @DisplayName("Получение всех MPA рейтингов → возвращает непустой список")
    void findAll_shouldReturnNonEmptyList() {
        List<Mpa> mpaList = mpaRepository.findAll();

        assertNotNull(mpaList);
        assertFalse(mpaList.isEmpty());
        assertTrue(mpaList.size() >= 1);
    }

    @Test
    @DisplayName("Получение всех MPA рейтингов → возвращает отсортированный по ID список")
    void findAll_shouldReturnSortedList() {
        List<Mpa> mpaList = mpaRepository.findAll();

        for (int i = 0; i < mpaList.size() - 1; i++) {
            assertTrue(mpaList.get(i).getId() <= mpaList.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("Получение MPA по существующему ID → возвращает Optional с MPA")
    void findById_withExistingId_shouldReturnMpa() {
        Optional<Mpa> foundMpa = mpaRepository.findById(1L);

        assertTrue(foundMpa.isPresent());
        assertEquals(1L, foundMpa.get().getId());
        assertEquals("G", foundMpa.get().getName());
    }

    @Test
    @DisplayName("Получение MPA по другому существующему ID → возвращает корректный MPA")
    void findById_withAnotherExistingId_shouldReturnCorrectMpa() {
        Optional<Mpa> mpa = mpaRepository.findById(2L);

        if (mpa.isPresent()) {
            assertEquals(2L, mpa.get().getId());
            assertNotNull(mpa.get().getName());
        }
    }

    @Test
    @DisplayName("Получение MPA по несуществующему ID → возвращает пустой Optional")
    void findById_withNonExistingId_shouldReturnEmptyOptional() {
        Optional<Mpa> foundMpa = mpaRepository.findById(9999L);

        assertFalse(foundMpa.isPresent());
    }

    @Test
    @DisplayName("Получение MPA по null ID → возвращает пустой Optional")
    void findById_withNullId_shouldReturnEmptyOptional() {
        Optional<Mpa> foundMpa = mpaRepository.findById(null);

        assertFalse(foundMpa.isPresent());
    }

    @Test
    @DisplayName("Получение MPA по отрицательному ID → возвращает пустой Optional")
    void findById_withNegativeId_shouldReturnEmptyOptional() {
        Optional<Mpa> foundMpa = mpaRepository.findById(-1L);

        assertFalse(foundMpa.isPresent());
    }

    @Test
    @DisplayName("Проверка существования MPA по существующему ID → возвращает true")
    void existsById_withExistingId_shouldReturnTrue() {
        boolean exists = mpaRepository.existsById(1L);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Проверка существования MPA по другому существующему ID → возвращает true")
    void existsById_withAnotherExistingId_shouldReturnTrue() {
        boolean exists = mpaRepository.existsById(2L);

        assertTrue(exists);
    }

    @Test
    @DisplayName("Проверка существования MPA по несуществующему ID → возвращает false")
    void existsById_withNonExistingId_shouldReturnFalse() {
        boolean exists = mpaRepository.existsById(9999L);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Проверка существования MPA по null ID → возвращает false")
    void existsById_withNullId_shouldReturnFalse() {
        boolean exists = mpaRepository.existsById(null);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Проверка существования MPA по отрицательному ID → возвращает false")
    void existsById_withNegativeId_shouldReturnFalse() {
        boolean exists = mpaRepository.existsById(-1L);

        assertFalse(exists);
    }

    @Test
    @DisplayName("Список MPA → содержит стандартные рейтинги")
    void findAll_shouldContainStandardMpaRatings() {
        List<Mpa> mpaList = mpaRepository.findAll();

        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("G")));
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("PG")));
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("PG-13")));
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("R")));
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("NC-17")));
    }

    @Test
    @DisplayName("Получение MPA по ID → возвращает корректные данные")
    void findById_shouldReturnCorrectData() {
        Optional<Mpa> mpaOpt = mpaRepository.findById(1L);

        assertTrue(mpaOpt.isPresent());
        Mpa mpa = mpaOpt.get();
        assertEquals(1L, mpa.getId());
        assertEquals("G", mpa.getName());
    }

    @Test
    @DisplayName("Список MPA → имеет правильное количество элементов")
    void findAll_shouldHaveCorrectNumberOfElements() {
        List<Mpa> mpaList = mpaRepository.findAll();

        assertTrue(mpaList.size() >= 5);
    }

    @Test
    @DisplayName("Повторный вызов findAll → возвращает одинаковые результаты")
    void findAll_multipleCalls_shouldReturnSameResults() {
        List<Mpa> firstCall = mpaRepository.findAll();
        List<Mpa> secondCall = mpaRepository.findAll();

        assertEquals(firstCall.size(), secondCall.size());
        for (int i = 0; i < firstCall.size(); i++) {
            assertEquals(firstCall.get(i).getId(), secondCall.get(i).getId());
            assertEquals(firstCall.get(i).getName(), secondCall.get(i).getName());
        }
    }

    @Test
    @DisplayName("Повторный вызов findById → возвращает одинаковые результаты")
    void findById_multipleCalls_shouldReturnSameResults() {
        Optional<Mpa> firstCall = mpaRepository.findById(1L);
        Optional<Mpa> secondCall = mpaRepository.findById(1L);

        assertEquals(firstCall.isPresent(), secondCall.isPresent());
        if (firstCall.isPresent() && secondCall.isPresent()) {
            assertEquals(firstCall.get().getId(), secondCall.get().getId());
            assertEquals(firstCall.get().getName(), secondCall.get().getName());
        }
    }

    @Test
    @DisplayName("Повторный вызов existsById → возвращает одинаковые результаты")
    void existsById_multipleCalls_shouldReturnSameResults() {
        boolean firstCall = mpaRepository.existsById(1L);
        boolean secondCall = mpaRepository.existsById(1L);

        assertEquals(firstCall, secondCall);
    }

    @Test
    @DisplayName("Получение MPA по граничным значениям ID")
    void findById_withBoundaryValues() {
        Optional<Mpa> minMpa = mpaRepository.findById(1L);
        assertTrue(minMpa.isPresent());

        List<Mpa> allMpa = mpaRepository.findAll();
        if (!allMpa.isEmpty()) {
            Long maxId = allMpa.get(allMpa.size() - 1).getId();
            Optional<Mpa> maxMpa = mpaRepository.findById(maxId);
            assertTrue(maxMpa.isPresent());
        }

        Optional<Mpa> beyondMax = mpaRepository.findById(100L);
        assertFalse(beyondMax.isPresent());
    }
}