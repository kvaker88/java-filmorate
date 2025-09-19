package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Collection<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director getById(Long id) {
        return directorStorage.getById(id);
    }

    public Director createDirector(Director director) {
        log.info("Запрос создания режиссера: {}", director);
        directorStorage.addDirector(director);
        log.info("Режиссер успешно создан. ID: {}, Имя: {}", director.getId(), director.getName());
        return director;
    }

    public Director updateDirector(Director director) {
        log.info("Запрос обновления режиссера: {}", director);
        if (!directorStorage.doesDirectorExist(director.getId())) {
            throw new NotFoundException("Режиссер не найден");
        }

        directorStorage.updateDirector(director);
        log.info("Режиссер успешно обновлен. ID: {}, Имя: {}", director.getId(), director.getName());
        return director;
    }

    public void removeDirectorById(Long directorId) {

        log.info("Запрос на удаление режиссера с id {}", directorId);

        if (!directorStorage.doesDirectorExist(directorId)) {
            throw new NotFoundException("Режиссер не найден");
        }

        directorStorage.removeDirectorById(directorId);

        log.info("Режиссер с id {} удален", directorId);
    }

}
