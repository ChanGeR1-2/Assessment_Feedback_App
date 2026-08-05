CREATE TABLE tag
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_tag_name UNIQUE (name)
);

CREATE TABLE feedback_tag
(
    id          BIGSERIAL PRIMARY KEY,
    feedback_id BIGINT      NOT NULL,
    tag_id      BIGINT      NOT NULL,
    tag_type    VARCHAR(20) NOT NULL,
    CONSTRAINT uk_feedback_tag UNIQUE (feedback_id, tag_id),
    CONSTRAINT fk_feedback_tag_feedback
        FOREIGN KEY (feedback_id) REFERENCES feedback (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_tag_tag
        FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
);

CREATE INDEX idx_feedback_tag_tag ON feedback_tag (tag_id);

INSERT INTO tag (name)
VALUES ('Referencing'),
       ('Structure'),
       ('Critical analysis'),
       ('Use of evidence'),
       ('Clarity of writing'),
       ('Depth of research'),
       ('Following the brief'),
       ('Technical accuracy'),
       ('Argument development'),
       ('Presentation'),
       ('Time management'),
       ('Originality');