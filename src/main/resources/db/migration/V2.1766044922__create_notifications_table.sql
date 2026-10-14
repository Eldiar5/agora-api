CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,

                               recipient_id BIGINT NOT NULL,
                               sender_login VARCHAR(255) NOT NULL,

    -- Оставляем NULL, чтобы в будущем уведомлять о подписках или системных событиях
                               target_id BIGINT,

                               message VARCHAR(255) NOT NULL,
                               type VARCHAR(50) NOT NULL, -- LIKE, COMMENT, SUBSCRIBE

                               is_read BOOLEAN DEFAULT FALSE NOT NULL,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ВАЖНО: Индекс для быстрого поиска уведомлений конкретного юзера.
-- Без него, когда уведомлений станет 100к+, лента уведомлений будет грузиться вечность.
CREATE INDEX idx_notifications_recipient_created ON notifications(recipient_id, created_at DESC);

-- Индекс для подсчета непрочитанных (чтобы факел загорался быстро)
CREATE INDEX idx_notifications_recipient_read ON notifications(recipient_id, is_read);