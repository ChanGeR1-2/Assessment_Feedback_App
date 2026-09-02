package com.dissertation.backend.feedback_audio;

import com.dissertation.backend.config.AppUserDetails;
import org.apache.coyote.Response;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/feedback/{feedbackId}/audio")
public class FeedbackAudioController {

    private final FeedbackAudioService feedbackAudioService;
    private final AudioStorageService audioStorageService;

    public FeedbackAudioController(FeedbackAudioService feedbackAudioService,
                                   AudioStorageService audioStorageService) {
        this.feedbackAudioService = feedbackAudioService;
        this.audioStorageService = audioStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAudio(@PathVariable Long feedbackId,
                                       @RequestParam("file") MultipartFile file,
                                       @AuthenticationPrincipal AppUserDetails principal) {
        feedbackAudioService.saveAudio(feedbackId, file, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Resource> getAudio(@PathVariable Long feedbackId,
                                        @AuthenticationPrincipal AppUserDetails principal) {
        FeedbackAudio audio = feedbackAudioService.getAudioMetadata(feedbackId, principal);
        Resource resource = audioStorageService.load(audio.getFilename());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(audio.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAudio(@PathVariable Long feedbackId, @AuthenticationPrincipal AppUserDetails principal) {
        feedbackAudioService.deleteAudio(feedbackId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}