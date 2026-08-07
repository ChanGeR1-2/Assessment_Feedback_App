package com.dissertation.backend.tags;

import com.dissertation.backend.feedback.FeedbackStatus;
import com.dissertation.backend.tags.dto.TagCountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackTagRepository extends JpaRepository<FeedbackTag, Long> {
    @Query("SELECT new com.dissertation.backend.tags.dto.TagCountResponse(t.name, ft.tagType, COUNT(ft)) " +
            "FROM FeedbackTag ft JOIN ft.tag t JOIN ft.feedback f " +
            "WHERE f.student.id = :studentId AND f.status = :status " +
            "GROUP BY t.name, ft.tagType " +
            "ORDER BY COUNT(ft) DESC")
    List<TagCountResponse> findTagCountsByStudent(@Param("studentId") Long studentId,
                                                  @Param("status") FeedbackStatus status);
    @Query("SELECT new com.dissertation.backend.tags.dto.TagCountResponse(t.name, ft.tagType, COUNT(ft)) " +
            "FROM FeedbackTag ft JOIN ft.tag t JOIN ft.feedback f " +
            "WHERE f.lecturer.id = :lecturerId " +
            "GROUP BY t.name, ft.tagType " +
            "ORDER BY COUNT(ft) DESC")
    List<TagCountResponse> findTagCountsByLecturer(@Param("lecturerId") Long lecturerId);
}
