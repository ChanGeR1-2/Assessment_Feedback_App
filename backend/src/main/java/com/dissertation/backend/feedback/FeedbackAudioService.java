package com.dissertation.backend.feedback;

import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.exceptions.AudioExistsException;
import com.dissertation.backend.feedback.exceptions.AudioNotFoundException;
import com.dissertation.backend.feedback.exceptions.FeedbackNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@Service
public class FeedbackAudioService {
    private final FeedbackAudioRepository feedbackAudioRepository;
    private final AudioStorageService audioStorageService;
    private final FeedbackRepository feedbackRepository;

    public FeedbackAudioService(FeedbackAudioRepository feedbackAudioRepository, AudioStorageService audioStorageService, FeedbackRepository feedbackRepository) {
        this.feedbackAudioRepository = feedbackAudioRepository;
        this.audioStorageService = audioStorageService;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public void saveAudio(Long feedbackId, MultipartFile file, Long lecturerId) {
        Feedback feedback = feedbackRepository.findByIdWithDetails(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        if (feedback.getLecturer() == null
                || !Objects.equals(feedback.getLecturer().getId(), lecturerId)) {
            throw new ForbiddenException("You are not authorised to upload audio for this feedback.");
        }

        if (feedbackAudioRepository.existsByFeedbackId(feedbackId)) {
            throw new AudioExistsException(feedbackId);
        }

        String filename = audioStorageService.store(file);
        try {
            feedbackAudioRepository.save(
                    new FeedbackAudio(feedback, filename, file.getContentType(), file.getSize()));
        } catch (RuntimeException e) {
            audioStorageService.delete(filename);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public FeedbackAudio getAudioMetadata(Long feedbackId, AppUserDetails principal) {
        Feedback feedback = feedbackRepository.findByIdWithDetails(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        boolean allowed = switch (principal.getRole()) {
            case LECTURER -> feedback.getLecturer() != null
                    && Objects.equals(feedback.getLecturer().getId(), principal.getId());
            case STUDENT -> Objects.equals(feedback.getStudent().getId(), principal.getId());
            default -> false;
        };
        if (!allowed) {
            throw new ForbiddenException("You are not authorised to view this feedback.");
        }

        return feedbackAudioRepository.findByFeedbackId(feedbackId)
                .orElseThrow(() -> new AudioNotFoundException(feedbackId));
    }
}
