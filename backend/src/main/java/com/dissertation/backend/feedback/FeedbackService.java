package com.dissertation.backend.feedback;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.app_users.UserRepository;
import com.dissertation.backend.app_users.UserRole;
import com.dissertation.backend.app_users.exceptions.UserNotFoundException;
import com.dissertation.backend.assessments.Assessment;
import com.dissertation.backend.assessments.AssessmentRepository;
import com.dissertation.backend.assessments.exceptions.AssessmentNotFoundException;
import com.dissertation.backend.common.exceptions.InvalidRoleException;
import com.dissertation.backend.course_modules.CourseModule;
import com.dissertation.backend.enrolment.EnrolmentRepository;
import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.feedback.dto.FeedbackResponse;
import com.dissertation.backend.feedback.exceptions.FeedbackExistsException;
import com.dissertation.backend.feedback.exceptions.FeedbackNotFoundException;
import com.dissertation.backend.feedback.exceptions.StudentNotEnrolledException;
import com.dissertation.backend.feedback.exceptions.UnauthorisedLecturerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final EnrolmentRepository enrolmentRepository;
    public FeedbackService(FeedbackRepository feedbackRepository, AssessmentRepository assessmentRepository, UserRepository userRepository, EnrolmentRepository enrolmentRepository) {
        this.feedbackRepository = feedbackRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
        this.enrolmentRepository = enrolmentRepository;
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getAssessment().getId(),
                feedback.getStudent().getId(),
                feedback.getLecturer().getId(),
                feedback.getMark().intValue(),
                feedback.getStrengths(),
                feedback.getImprovements(),
                feedback.getActions(),
                feedback.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByStudentId(Long studentId) {
        AppUser student = userRepository.findAppUserById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));

        if (student.getRole() != UserRole.STUDENT) {
            throw new InvalidRoleException(student.getId(), UserRole.STUDENT);
        }

        List<Feedback> feedbacks = feedbackRepository.findByStudentId(studentId);
        return feedbacks.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .map(this::toResponse)
                .orElseThrow(() -> new FeedbackNotFoundException(feedbackId));
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackByAssessmentIdAndStudentId(Long studentId, Long assessmentId) {
        return feedbackRepository.findByAssessmentIdAndStudentId(assessmentId, studentId)
                .map(this::toResponse)
                .orElseThrow(() -> new FeedbackNotFoundException(assessmentId, studentId));
    }

    // TODO: SECURITY
    @Transactional
    public FeedbackResponse saveFeedback(CreateFeedbackRequest createFeedbackRequest, Long lecturerId) {
        AppUser student = userRepository.findAppUserById(createFeedbackRequest.studentId())
                .orElseThrow(() -> new UserNotFoundException(createFeedbackRequest.studentId()));

        if (student.getRole() != UserRole.STUDENT) {
            throw new InvalidRoleException(student.getId(), UserRole.STUDENT);
        }

        Assessment assessment = assessmentRepository.findAssessmentById(createFeedbackRequest.assessmentId())
                .orElseThrow(() -> new AssessmentNotFoundException(createFeedbackRequest.assessmentId()));

        CourseModule module = assessment.getModule();

        AppUser lecturer = userRepository.findAppUserById(lecturerId)
                .orElseThrow(() -> new UserNotFoundException(lecturerId));

        if (lecturer.getRole() != UserRole.LECTURER) {
            throw new InvalidRoleException(lecturer.getId(), UserRole.LECTURER);
        }

        if (!Objects.equals(module.getLecturer().getId(), lecturer.getId())) {
            throw new UnauthorisedLecturerException(lecturerId, module.getId());
        }

        if (feedbackRepository.existsByAssessmentIdAndStudentId(assessment.getId(), student.getId())) {
            throw new FeedbackExistsException(assessment.getId(), student.getId());
        }

        if (!enrolmentRepository.existsByStudentIdAndModuleId(student.getId(), module.getId())) {
            throw new StudentNotEnrolledException(student.getId(), module.getId());
        }

        Feedback feedback = new Feedback(student, lecturer, assessment, createFeedbackRequest.mark(), createFeedbackRequest.strengths(), createFeedbackRequest.improvements(), createFeedbackRequest.actions());

        Feedback saved = feedbackRepository.save(feedback);

        return toResponse(saved);
    }
}
