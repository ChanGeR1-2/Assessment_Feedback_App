package com.dissertation.backend.phrases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePhraseRequest(
        @NotBlank @Size(max = 100) String label,
        @NotBlank @Size(max = 2000) String text
) {
}
