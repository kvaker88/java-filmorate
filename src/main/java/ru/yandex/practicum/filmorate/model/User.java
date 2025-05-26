package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private Long id;

    @Email(message = "Email должен содержать символ @")
    private String email;

    private String login;
    private String name;
    private LocalDate birthday;
}