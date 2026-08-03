CREATE TABLE feedback_phrase
(
    id          BIGSERIAL PRIMARY KEY,
    lecturer_id BIGINT       NOT NULL,
    label       VARCHAR(100) NOT NULL,
    text        TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_phrase_lecturer
        FOREIGN KEY (lecturer_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE INDEX idx_phrase_lecturer ON feedback_phrase (lecturer_id);
