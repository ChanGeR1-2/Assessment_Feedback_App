package com.dissertation.backend.phrases;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackPhraseRepository extends JpaRepository<FeedbackPhrase, Long> {
    List<FeedbackPhrase> findByLecturerIdOrderByLabelAsc(Long lecturerId);
}
