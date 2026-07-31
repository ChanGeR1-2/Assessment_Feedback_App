package com.dissertation.backend.feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackAudioRepository extends JpaRepository<FeedbackAudio, Long> {
    Optional<FeedbackAudio> findByFeedbackId(Long feedbackId);

    boolean existsByFeedbackId(Long feedbackId);
}
