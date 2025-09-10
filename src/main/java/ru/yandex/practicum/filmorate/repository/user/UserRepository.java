package ru.yandex.practicum.filmorate.repository.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.BaseRepository;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.repository.mapper.UserRowMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("userRepository")
@Primary
@Slf4j
public class UserRepository extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";

    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    private static final String INSERT_FRIENDSHIP_QUERY =
            "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
    private static final String INSERT_FRIEND_REQUEST_QUERY =
            "INSERT INTO friend_requests (sender_id, receiver_id) VALUES (?, ?)";
    private static final String DELETE_FRIENDSHIP_QUERY =
            "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_FRIEND_REQUEST_QUERY =
            "DELETE FROM friend_requests WHERE sender_id = ? AND receiver_id = ?";
    private static final String CHECK_FRIENDSHIP_QUERY =
            "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String CHECK_FRIEND_REQUEST_QUERY =
            "SELECT COUNT(*) FROM friend_requests WHERE sender_id = ? AND receiver_id = ?";
    private static final String GET_FRIENDS_QUERY =
            "SELECT friend_id FROM friendships WHERE user_id = ?";
    private static final String GET_OUTGOING_REQUESTS_QUERY =
            "SELECT receiver_id FROM friend_requests WHERE sender_id = ?";


    private static final String EXISTS_QUERY = "SELECT COUNT(*) FROM users WHERE id = ?";

    public UserRepository(JdbcTemplate jdbc, UserRowMapper userRowMapper) {
        super(jdbc, userRowMapper);
    }

    @Override
    public List<User> getAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User getUserById(Long userId) {
        return findOne(FIND_BY_ID_QUERY, userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    @Override
    public void addUser(User user) {
        long id = insert(INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        user.setId(id);
    }

    @Override
    public void updateUser(User user) {
        update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        if (friendshipExists(userId, friendId)) {
            log.warn("Дружба уже существует: {} -> {}", userId, friendId);
            return;
        }

        if (friendRequestExists(userId, friendId)) {
            log.warn("Заявка уже отправлена: {} -> {}", userId, friendId);
            return;
        }

        if (friendRequestExists(friendId, userId)) {
            jdbc.update(DELETE_FRIEND_REQUEST_QUERY, friendId, userId);
            jdbc.update(INSERT_FRIENDSHIP_QUERY, userId, friendId);
            return;
        }

        jdbc.update(INSERT_FRIEND_REQUEST_QUERY, userId, friendId);
        jdbc.update(INSERT_FRIENDSHIP_QUERY, userId, friendId);
        log.info("Пользователь {} отправил заявку в друзья пользователю {}", userId, friendId);
    }

    private boolean friendRequestExists(Long senderId, Long receiverId) {
        Integer count = jdbc.queryForObject(CHECK_FRIEND_REQUEST_QUERY, Integer.class, senderId, receiverId);
        return count != null && count > 0;
    }

    private boolean friendshipExists(Long userId, Long friendId) {
        Integer count = jdbc.queryForObject(CHECK_FRIENDSHIP_QUERY, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbc.update(DELETE_FRIENDSHIP_QUERY, userId, friendId);

        jdbc.update(DELETE_FRIEND_REQUEST_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        List<Long> friendIds = jdbc.query(GET_FRIENDS_QUERY,
                (rs, rowNum) -> rs.getLong("friend_id"), userId);

        return friendIds.stream()
                .map(this::getUserById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean doesUserNotExist(Long id) {
        Integer count = jdbc.queryForObject(EXISTS_QUERY, Integer.class, id);
        return count == null || count == 0;
    }

    public HashSet<Long> getFriendsById(Long userId) {
        Set<Long> allRelations = new HashSet<>();

        try {
            List<Long> friends = jdbc.query(GET_FRIENDS_QUERY,
                    (rs, rowNum) -> rs.getLong("friend_id"), userId);
            allRelations.addAll(friends);

            List<Long> outgoingRequests = jdbc.query(GET_OUTGOING_REQUESTS_QUERY,
                    (rs, rowNum) -> rs.getLong("receiver_id"), userId);
            allRelations.addAll(outgoingRequests);

            return new HashSet<>(allRelations);

        } catch (Exception e) {
            log.warn("Ошибка при получении друзей и заявок для пользователя {}: {}", userId, e.getMessage());
            return new HashSet<>();
        }
    }

    public List<Long> getFriendIds(Long userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("friend_id"), userId);
    }

    public boolean isFriendRequestExists(Long senderId, Long receiverId) {
        String sql = "SELECT COUNT(*) FROM friend_requests WHERE sender_id = ? AND receiver_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, senderId, receiverId);
        return count != null && count > 0;
    }

    public boolean isFriendshipExists(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}
