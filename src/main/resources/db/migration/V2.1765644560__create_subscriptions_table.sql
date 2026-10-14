CREATE TABLE subscriptions (
                               id BIGSERIAL PRIMARY KEY,
                               follower_id BIGINT NOT NULL,
                               following_id BIGINT NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Внешние ключи на таблицу пользователей
                               CONSTRAINT fk_subscriptions_follower FOREIGN KEY (follower_id) REFERENCES users (id),
                               CONSTRAINT fk_subscriptions_following FOREIGN KEY (following_id) REFERENCES users (id),

    -- Уникальное ограничение, чтобы нельзя было подписаться дважды на одного и того же
                               CONSTRAINT uq_subscriptions_follower_following UNIQUE (follower_id, following_id)
);