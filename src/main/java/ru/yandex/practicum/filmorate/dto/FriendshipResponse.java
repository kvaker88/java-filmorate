package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendshipResponse {
    private Long userId;
    private Long friendId;
    private String status;
    private LocalDateTime timestamp;

    public FriendshipResponse(Long userId, Long friendId, String status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
