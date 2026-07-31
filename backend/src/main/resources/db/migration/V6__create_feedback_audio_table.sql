CREATE TABLE feedback_audio
(
    id           BIGSERIAL PRIMARY KEY,
    feedback_id  BIGINT       NOT NULL,
    filename     VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_audio_feedback
        FOREIGN KEY (feedback_id) REFERENCES feedback (id) ON DELETE CASCADE,
    CONSTRAINT uk_audio_feedback UNIQUE (feedback_id)
);