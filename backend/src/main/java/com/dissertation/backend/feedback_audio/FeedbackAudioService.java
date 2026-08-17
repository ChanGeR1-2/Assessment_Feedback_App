package com.dissertation.backend.feedback_audio;

import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.feedback.Feedback;
import com.dissertation.backend.feedback.FeedbackRepository;
import com.dissertation.backend.feedback_audio.exceptions.AudioNotFoundException;
import com.dissertation.backend.feedback.exceptions.FeedbackNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

/**
 * Manages the storage and retrieval of feedback audio files.
 * <p> Authorisation is enforced here rather than in the controller, since access
 * depends on the data: a lecturer may only upload audio for feedback they authored, and
 * a student may only view audio on their own feedback.
 *
 * <p> Audio files are stored on a separate docker volume, and the metadata is stored in the database.
 * System file storage is handled by the {@link AudioStorageService}.
 */
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

    /**
     * Saves the audio file for the given feedback.
     * Persists the new audio file and metadata before deleting the existing one so that the delete is atomic.
     * @param feedbackId the feedback id
     * @param file the audio file
     * @param lecturerId the lecturer id
     * @throws ForbiddenException if the user is not authorised to upload audio for the feedback.
     * @throws FeedbackNotFoundException if the feedback does not exist.
     * @throws RuntimeException if there is an error saving the audio file to the database. Deletes the audio file before throwing the exception.
     */
    @Transactional
    public void saveAudio(Long feedbackId, MultipartFile file, Long lecturerId) {
        Feedback feedback = feedbackRepository.findByIdWithDetails(feedbackId)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));

        if (feedback.getLecturer() == null
                || !Objects.equals(feedback.getLecturer().getId(), lecturerId)) {
            throw new ForbiddenException("You are not authorised to upload audio for this feedback.");
        }

        Optional<FeedbackAudio> existing = feedbackAudioRepository.findByFeedbackId(feedbackId);

        String filename = audioStorageService.store(file);
        try {
            // Save the new file metadata before deleting the existing one so that the delete is atomic
            feedbackAudioRepository.save(
                    new FeedbackAudio(feedback, filename, file.getContentType(), file.getSize()));

            if (existing.isPresent()) {
                feedbackAudioRepository.delete(existing.get());
                feedbackAudioRepository.flush(); // flush to ensure the delete is committed
            }
        } catch (RuntimeException e) {
            audioStorageService.delete(filename); // delete the audio file if there's an error saving the metadata
            throw e;
        }
        existing.ifPresent(a -> audioStorageService.delete(a.getFilename()));
    }

    /**
     * Gets the audio metadata for a given feedback.
     * @param feedbackId the feedback id
     * @param principal the authenticated user - taken from the security context.
     * @return the audio metadata.
     * @throws ForbiddenException if the user is not authorised to view the feedback.
     * @throws FeedbackNotFoundException if the feedback does not exist.
     * @throws AudioNotFoundException if the feedback does not have an audio file.
     */
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
