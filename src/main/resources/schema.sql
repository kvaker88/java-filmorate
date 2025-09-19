CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS mpa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS films (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE,
    duration BIGINT,
    mpa_id BIGINT NULL REFERENCES mpa(id)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT REFERENCES films(id) ON DELETE CASCADE,
    genre_id BIGINT REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS film_likes (
    film_id BIGINT REFERENCES films(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS friendships (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    friend_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS friend_requests (
    sender_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    receiver_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (sender_id, receiver_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id   BIGSERIAL PRIMARY KEY,
    film_id     BIGINT     NOT NULL,
    user_id     BIGINT     NOT NULL,
    content     TEXT       NOT NULL,
    is_positive BOOLEAN    NOT NULL,
    useful      INT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMP  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP  NOT NULL DEFAULT NOW()
);


CREATE TABLE IF NOT EXISTS review_votes (
    review_id BIGINT    NOT NULL,
    user_id   BIGINT    NOT NULL,
    value     SMALLINT  NOT NULL,
    CONSTRAINT ck_review_votes_value CHECK (value IN (-1, 1)),
    CONSTRAINT pk_review_votes PRIMARY KEY (review_id, user_id),
    CONSTRAINT fk_review_votes_review FOREIGN KEY (review_id) REFERENCES reviews (review_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_votes_user   FOREIGN KEY (user_id)   REFERENCES users (user_id)   ON DELETE CASCADE
);
