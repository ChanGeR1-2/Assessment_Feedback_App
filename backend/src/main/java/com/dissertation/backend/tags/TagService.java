package com.dissertation.backend.tags;

import com.dissertation.backend.tags.dto.TagResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
