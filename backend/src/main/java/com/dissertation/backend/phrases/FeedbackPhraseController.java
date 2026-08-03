package com.dissertation.backend.phrases;

import com.dissertation.backend.config.AppUserDetails;
import com.dissertation.backend.phrases.dto.CreatePhraseRequest;
import com.dissertation.backend.phrases.dto.PhraseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phrases")
public class FeedbackPhraseController {

    private final FeedbackPhraseService phraseService;

    public FeedbackPhraseController(FeedbackPhraseService phraseService) {
        this.phraseService = phraseService;
    }

    @GetMapping
    public List<PhraseResponse> getMyPhrases(@AuthenticationPrincipal AppUserDetails principal) {
        return phraseService.getMyPhrases(principal.getId());
    }

    @PostMapping
    public ResponseEntity<PhraseResponse> create(@Valid @RequestBody CreatePhraseRequest request,
                                                 @AuthenticationPrincipal AppUserDetails principal) {
        return new ResponseEntity<>(phraseService.create(request, principal.getId()), HttpStatus.CREATED);
    }

    @PutMapping("/{phraseId}")
    public PhraseResponse update(@PathVariable Long phraseId,
                                 @Valid @RequestBody CreatePhraseRequest request,
                                 @AuthenticationPrincipal AppUserDetails principal) {
        return phraseService.update(phraseId, request, principal.getId());
    }

    @DeleteMapping("/{phraseId}")
    public ResponseEntity<Void> delete(@PathVariable Long phraseId,
                                       @AuthenticationPrincipal AppUserDetails principal) {
        phraseService.delete(phraseId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}