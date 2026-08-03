package com.dissertation.backend.phrases;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.phrases.dto.CreatePhraseRequest;
import com.dissertation.backend.phrases.dto.PhraseResponse;
import com.dissertation.backend.phrases.exceptions.PhraseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class FeedbackPhraseService {

    private final FeedbackPhraseRepository phraseRepository;
    private final UserRepository userRepository;

    public FeedbackPhraseService(FeedbackPhraseRepository phraseRepository, UserRepository userRepository) {
        this.phraseRepository = phraseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PhraseResponse> getMyPhrases(Long lecturerId) {
        return phraseRepository.findByLecturerIdOrderByLabelAsc(lecturerId).stream()
                .map(p -> new PhraseResponse(p.getId(), p.getLabel(), p.getText()))
                .toList();
    }

    @Transactional
    public PhraseResponse create(CreatePhraseRequest request, Long lecturerId) {
        AppUser lecturer = userRepository.findAppUserById(lecturerId)
                .orElseThrow(() -> new UserNotFoundException(lecturerId));
        FeedbackPhrase phrase = phraseRepository.save(
                new FeedbackPhrase(lecturer, request.label(), request.text()));
        return new PhraseResponse(phrase.getId(), phrase.getLabel(), phrase.getText());
    }

    @Transactional
    public PhraseResponse update(Long phraseId, CreatePhraseRequest request, Long lecturerId) {
        FeedbackPhrase phrase = requireOwned(phraseId, lecturerId);
        phrase.setLabel(request.label());
        phrase.setText(request.text());
        return new PhraseResponse(phrase.getId(), phrase.getLabel(), phrase.getText());
    }

    @Transactional
    public void delete(Long phraseId, Long lecturerId) {
        phraseRepository.delete(requireOwned(phraseId, lecturerId));
    }

    private FeedbackPhrase requireOwned(Long phraseId, Long lecturerId) {
        FeedbackPhrase phrase = phraseRepository.findById(phraseId)
                .orElseThrow(() -> new PhraseNotFoundException(phraseId));
        if (!Objects.equals(phrase.getLecturer().getId(), lecturerId)) {
            throw new ForbiddenException("You are not authorised to modify this phrase.");
        }
        return phrase;
    }
}
