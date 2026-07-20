CREATE TABLE marking_item (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    max_mark SMALLINT NOT NULL CHECK (max_mark > 0),
    position SMALLINT NOT NULL CHECK (position >= 0),
    CONSTRAINT fk_marking_item_assessment
        FOREIGN KEY (assessment_id) REFERENCES assessment(id)
            ON DELETE CASCADE,
    CONSTRAINT uk_marking_item_assessment_name
        UNIQUE (assessment_id, name)
);

CREATE INDEX idx_marking_item_assessment_id ON marking_item(assessment_id);

CREATE table feedback_item (
    id BIGSERIAL PRIMARY KEY,
    feedback_id BIGINT NOT NULL,
    marking_item_id BIGINT NOT NULL,
    awarded_mark SMALLINT NOT NULL CHECK (awarded_mark >= 0),
    comment TEXT,
    CONSTRAINT fk_feedback_item_marking_item
        FOREIGN KEY (marking_item_id) REFERENCES marking_item(id)
            ON DELETE CASCADE,
    CONSTRAINT fk_feedback_item_feedback
        FOREIGN KEY (feedback_id) REFERENCES feedback(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_feedback_item_feedback_id ON feedback_item(feedback_id);
CREATE INDEX idx_feedback_item_marking_item_id ON feedback_item(marking_item_id);

ALTER TABLE feedback DROP COLUMN strengths;
ALTER TABLE feedback DROP COLUMN improvements;
ALTER TABLE feedback DROP COLUMN actions;
ALTER TABLE feedback ADD COLUMN summary TEXT;