-- src/main/resources/schema.sql

-- Очистка при старте (H2 допускает IF EXISTS)
DROP TABLE IF EXISTS review_feedback;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS friendships;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS mpa;

-- Справочники
CREATE TABLE IF NOT EXISTS mpa (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Основные сущности
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_id INT NOT NULL,
    CONSTRAINT fk_films_mpa FOREIGN KEY (mpa_id) REFERENCES mpa(id)
);

-- связи фильмов и жанров
CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT NOT NULL,
    genre_id INT NOT NULL,
    CONSTRAINT pk_film_genres PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_fg_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    CONSTRAINT fk_fg_genre FOREIGN KEY (genre_id) REFERENCES genres(id)
);

-- лайки фильмов
CREATE TABLE IF NOT EXISTS likes (
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_likes PRIMARY KEY (film_id, user_id),
    CONSTRAINT fk_likes_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- дружба/заявки в друзья (строка статуса позволяет "REQUESTED"/"CONFIRMED")
CREATE TABLE IF NOT EXISTS friendships (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL, -- REQUESTED / CONFIRMED
    CONSTRAINT pk_friendships PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_f_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_f_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ----------- ОТЗЫВЫ -----------
CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(1000) NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    useful INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_film FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
);

-- Отклики пользователей на отзывы: like=+1 / dislike=-1
CREATE TABLE IF NOT EXISTS review_feedback (
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    value SMALLINT NOT NULL CHECK (value IN (-1, 1)),
    CONSTRAINT pk_review_feedback PRIMARY KEY (review_id, user_id),
    CONSTRAINT fk_rf_review FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
    CONSTRAINT fk_rf_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id IDENTITY PRIMARY KEY,
    content   VARCHAR(10000) NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id   BIGINT NOT NULL,
    film_id   BIGINT NOT NULL
    -- FK намеренно не задаём, чтобы не поломать чужую схему/тесты.
);

CREATE TABLE IF NOT EXISTS review_reactions (
    review_id BIGINT NOT NULL,
    user_id   BIGINT NOT NULL,
    is_like   BOOLEAN NOT NULL,
    CONSTRAINT pk_review_reactions PRIMARY KEY (review_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_film_id ON reviews(film_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id);