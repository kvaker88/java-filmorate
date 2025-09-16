package ru.yandex.practicum.filmorate.repository.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class UserRepository implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    // ===== CRUD =====

    @Override
    public void addUser(User user) {
        final String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(id);
    }

    @Override
    public void updateUser(User user) {
        final String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
    }

    @Override
    public User getUserById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, email, login, name, birthday FROM users WHERE id = ?",
                    userRowMapper,
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null; // сервис бросит NotFound, см. doesUserNotExist(...)
        }
    }

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query(
                "SELECT id, email, login, name, birthday FROM users ORDER BY id",
                userRowMapper
        );
    }

    // ===== FRIENDS =====

    @Override
    public void addFriend(Long userId, Long friendId) {
        // Связь направленная. Дубликаты не создаём.
        jdbcTemplate.update(
                "INSERT INTO user_friends (user_id, friend_id) " +
                        "SELECT ?, ? WHERE NOT EXISTS (" +
                        "    SELECT 1 FROM user_friends WHERE user_id = ? AND friend_id = ?" +
                        ")",
                userId, friendId, userId, friendId
        );
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbcTemplate.update(
                "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );
    }

    @Override
    public List<User> getFriends(Long userId) {
        // делегируем на уже реализованный метод
        return getFriendsByUserId(userId);
    }

    @Override
    public List<Long> getFriendIds(Long userId) {
        final String sql = "SELECT friend_id FROM user_friends WHERE user_id = ? ORDER BY friend_id";
        return jdbcTemplate.query(sql, (rs, rn) -> rs.getLong(1), userId);
    }

    @Override
    public List<User> getFriendsByUserId(Long userId) {
        final String sql =
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM users u " +
                        "JOIN user_friends f ON f.friend_id = u.id " +
                        "WHERE f.user_id = ? " +
                        "ORDER BY u.id";
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        final String sql =
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM users u " +
                        "JOIN user_friends f1 ON f1.friend_id = u.id AND f1.user_id = ? " +
                        "JOIN user_friends f2 ON f2.friend_id = u.id AND f2.user_id = ? " +
                        "ORDER BY u.id";
        return jdbcTemplate.query(sql, userRowMapper, userId, otherId);
    }

    // ===== EXISTS / DELETE =====

    @Override
    public boolean existsById(long id) {
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Integer.class, id
        );
        return cnt != null && cnt > 0;
    }

    @Override
    public boolean doesUserNotExist(Long id) {
        return id == null || !existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        // 1) рвём дружбу в обе стороны
        jdbcTemplate.update("DELETE FROM user_friends WHERE user_id = ? OR friend_id = ?", id, id);

        // 2) удаляем лайки пользователя к фильмам
        jdbcTemplate.update("DELETE FROM likes WHERE user_id = ?", id);

        // 3) сам пользователь
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }
}