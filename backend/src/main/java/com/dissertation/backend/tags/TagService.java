package com.dissertation.backend.tags;

import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.common.exceptions.ForbiddenException;
import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.feedback.FeedbackStatus;
import com.dissertation.backend.tags.dto.TagCountResponse;
import com.dissertation.backend.tags.dto.TagResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final FeedbackTagRepository feedbackTagRepository;
    public TagService(TagRepository tagRepository, FeedbackTagRepository feedbackTagRepository) {
        this.tagRepository = tagRepository;
        this.feedbackTagRepository = feedbackTagRepository;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map((t) -> new TagResponse(t.getId(), t.getName()))
                .toList();
    }
    @Transactional(readOnly = true)
    public List<TagCountResponse> getStudentTagCounts(Long studentId, AppUserDetails principal) {
        if (principal.getRole() == UserRole.STUDENT
                && !Objects.equals(principal.getId(), studentId)) {
            throw new ForbiddenException("You are not authorised to view this summary.");
        }
        return feedbackTagRepository.findTagCountsByStudent(studentId, FeedbackStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public List<TagCountResponse> getLecturerTagCounts(Long lecturerId) {
        return feedbackTagRepository.findTagCountsByLecturer(lecturerId);
    }

}
