package com.dissertation.backend.feedback_audio;

import com.dissertation.backend.feedback_audio.exceptions.AudioNotFoundException;
import com.dissertation.backend.feedback_audio.exceptions.AudioStorageException;
import com.dissertation.backend.feedback_audio.exceptions.InvalidAudioException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the storage and retrieval of feedback audio files.
 */
@Service
public class AudioStorageService {

    private final Path storageRoot;
    private static final Set<String> ALLOWED = Set.of("audio/webm", "audio/ogg", "audio/mpeg", "audio/mp4");
    private static final long MAX_BYTES = 10 * 1024 * 1024; // 10 MB


    public AudioStorageService(@Value("${app.audio.storage-path}") String path) {
        this.storageRoot = Paths.get(path).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(storageRoot);
    }

    /**
     * Stores the audio file and returns the filename.
     * @param file the audio file
     * @return the filename
     * @throws InvalidAudioException if the file is empty, exceeds 10 MB, or is not an allowed audio format.
     * @throws AudioStorageException if there is an error storing the file.
     */
    public String store(MultipartFile file) {
        if (file.isEmpty()) throw new InvalidAudioException("File is empty");
        if (file.getSize() > MAX_BYTES) throw new InvalidAudioException("File exceeds 10 MB");
        if (!ALLOWED.contains(file.getContentType())) {
            throw new InvalidAudioException("Unsupported audio format: " + file.getContentType());
        }

        String filename = UUID.randomUUID() + extensionFor(file.getContentType());
        try {
            Path target = storageRoot.resolve(filename).normalize();
            if (!target.startsWith(storageRoot)) {
                throw new InvalidAudioException("Invalid filename");
            }
            file.transferTo(target);
            return filename;
        } catch (IOException e) {
            throw new AudioStorageException("Failed to store audio", e);
        }
    }

    /**
     * Loads the audio file from the storage.
     * @param filename the filename
     * @return the audio file
     * @throws AudioNotFoundException if the file does not exist.
     * @throws InvalidAudioException if the filename is invalid.
     */
    public Resource load(String filename) {
        try {
            Path file = storageRoot.resolve(filename).normalize();
            if (!file.startsWith(storageRoot)) throw new InvalidAudioException("Invalid filename");
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new AudioNotFoundException(filename);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new AudioNotFoundException(filename);
        }
    }

    public void delete(String filename) {
        try {
            Files.deleteIfExists(storageRoot.resolve(filename).normalize());
        } catch (IOException e) {

        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "audio/webm" -> ".webm";
            case "audio/ogg" -> ".ogg";
            case "audio/mpeg" -> ".mp3";
            case "audio/mp4" -> ".m4a";
            default -> "";
        };
    }
}