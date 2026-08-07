package com.dissertation.backend.tags;

import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.tags.dto.TagCountResponse;
import com.dissertation.backend.tags.dto.TagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TagController {
    private final TagService tagService;
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/students/{studentId}/tag-summary")
    public List<TagCountResponse> studentSummary(@PathVariable Long studentId,
                                                 @AuthenticationPrincipal AppUserDetails principal) {
        return tagService.getStudentTagCounts(studentId, principal);
    }

    @GetMapping("/lecturer/tag-summary")
    public List<TagCountResponse> lecturerSummary(@AuthenticationPrincipal AppUserDetails principal) {
        return tagService.getLecturerTagCounts(principal.getId());
    }
}
