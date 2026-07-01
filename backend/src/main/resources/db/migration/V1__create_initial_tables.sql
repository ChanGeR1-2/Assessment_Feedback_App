CREATE TABLE app_user
(
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE course_module
(
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    code          VARCHAR(255) NOT NULL,
    academic_year VARCHAR(9)   NOT NULL,
    lecturer_id   BIGINT,
    CONSTRAINT fk_module_lecturer
        FOREIGN KEY (lecturer_id)
            REFERENCES app_user (id)
            ON DELETE SET NULL,
    CONSTRAINT uk_module_code_academic_year
        UNIQUE (code, academic_year)

);

CREATE INDEX idx_module_lecturer_id ON course_module (lecturer_id);

CREATE TABLE assessment
(
    id        BIGSERIAL PRIMARY KEY,
    module_id BIGINT       NOT NULL,
    title     VARCHAR(255) NOT NULL,
    due_date  TIMESTAMP,
    CONSTRAINT fk_assessment_module
        FOREIGN KEY (module_id)
            REFERENCES course_module (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_assessment_module ON assessment (module_id);

CREATE TABLE feedback
(
    id            BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT    NOT NULL,
    student_id    BIGINT    NOT NULL,
    lecturer_id   BIGINT    NOT NULL,
    mark          SMALLINT  NOT NULL CHECK (mark >= 0 AND mark <= 100),
    strengths     TEXT,
    improvements  TEXT,
    actions       TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_assessment
        FOREIGN KEY (assessment_id)
            REFERENCES assessment (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_feedback_student
        FOREIGN KEY (student_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_feedback_lecturer
        FOREIGN KEY (lecturer_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_feedback_assessment_student
        UNIQUE (assessment_id, student_id)
);

CREATE INDEX idx_feedback_assessment_id ON feedback (assessment_id);
CREATE INDEX idx_feedback_student_id ON feedback (student_id);
CREATE INDEX idx_feedback_lecturer_id ON feedback (lecturer_id);
