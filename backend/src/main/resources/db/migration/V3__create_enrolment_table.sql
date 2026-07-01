CREATE TABLE enrolment
(
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT    NOT NULL,
    module_id   BIGINT    NOT NULL,
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrolment_student
        FOREIGN KEY (student_id) REFERENCES app_user (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_enrolment_module
        FOREIGN KEY (module_id) REFERENCES course_module (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_enrolment_student_module
        UNIQUE (student_id, module_id)
);

CREATE INDEX idx_enrolment_student_id ON enrolment (student_id);
CREATE INDEX idx_enrolment_module_id ON enrolment (module_id);
