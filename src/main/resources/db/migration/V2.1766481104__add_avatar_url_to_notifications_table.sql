ALTER TABLE notifications
    ADD COLUMN sender_avatar_url VARCHAR(512);
-- VARCHAR(512) с запасом, так как ссылки на MinIO могут быть длинными