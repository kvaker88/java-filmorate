package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.repository.film.FilmRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final FilmRepository filmRepository;
    private final UserStorage userStorage;

    public List<Film> getRecommendations(Long userId) {
        log.info("Получение рекомендаций для пользователя {}", userId);

        if (userStorage.doesUserNotExist(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Set<Long> userLikes = new HashSet<>(filmRepository.getUserLikedFilmIds(userId));

        if (userLikes.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Set<Long>> allUserLikes = filmRepository.getAllUserLikes();
        Map<Long, Integer> filmScores = new HashMap<>();

        for (Long likedFilm : userLikes) {
            Set<Long> usersWhoLikedThis = new HashSet<>();
            for (Map.Entry<Long, Set<Long>> entry : allUserLikes.entrySet()) {
                if (entry.getValue().contains(likedFilm)) {
                    usersWhoLikedThis.add(entry.getKey());
                }
            }

            for (Long similarUserId : usersWhoLikedThis) {
                if (similarUserId.equals(userId)) continue;

                Set<Long> otherFilms = allUserLikes.get(similarUserId);
                for (Long candidateFilm : otherFilms) {
                    if (!userLikes.contains(candidateFilm)) {
                        filmScores.merge(candidateFilm, 1, Integer::sum);
                    }
                }
            }
        }

        List<Long> recommendedIds = filmScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("Рекомендуем {} фильмов пользователю {}", recommendedIds.size(), userId);
        return filmRepository.getFilmsByIds(recommendedIds);
    }
}