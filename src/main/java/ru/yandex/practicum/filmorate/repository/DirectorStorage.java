package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {

    Collection<Director> getAll();

    Director getById(Long directorId);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    boolean removeDirectorById(Long directorId);

    boolean doesDirectorExist(Long directorId);
}
