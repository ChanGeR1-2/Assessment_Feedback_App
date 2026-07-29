CREATE TABLE feedback_query
(
    id          BIGSERIAL PRIMARY KEY,
    query       TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    feedback_id BIGINT    NOT NULL,
    student_id  BIGINT    NOT NULL,
    CONSTRAINT fk_query_feedback
        FOREIGN KEY (feedback_id) REFERENCES feedback (id) ON DELETE CASCADE,
    CONSTRAINT fk_query_student
        FOREIGN KEY (student_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT uk_query_feedback
        UNIQUE (feedback_id)
);

CREATE INDEX idx_query_feedback ON feedback_query (feedback_id);
CREATE INDEX idx_query_student ON feedback_query (student_id);

CREATE TABLE feedback_query_answer
(
    id                BIGSERIAL PRIMARY KEY,
    feedback_query_id BIGINT    NOT NULL,
    answer          TEXT      NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    lecturer_id       BIGINT    NOT NULL,
    CONSTRAINT fk_answer_query
        FOREIGN KEY (feedback_query_id) REFERENCES feedback_query (id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_lecturer
        FOREIGN KEY (lecturer_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT uk_answer_query
        UNIQUE (feedback_query_id)
);