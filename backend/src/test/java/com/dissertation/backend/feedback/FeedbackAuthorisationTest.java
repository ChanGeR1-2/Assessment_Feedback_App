package com.dissertation.backend.feedback;

import com.dissertation.backend.app_users.AppUser;
import com.dissertation.backend.feedback.dto.CreateFeedbackItemRequest;
import com.dissertation.backend.feedback.dto.CreateFeedbackRequest;
import com.dissertation.backend.testsupport.TestDataBuilder;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication and authorisation, chosen to test both
 * security chain filter rules as well as authorisation rules
 * enforced by the different services.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FeedbackAuthorisationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private TestDataBuilder data;

    private String tokenFor(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"%s","password":"password123"}""".formatted(email)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/feedback/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentCanReadOwnFeedback() throws Exception {
        var scenario = data.scenario();
        var feedback = data.publishedFeedback(
                scenario.student(), scenario.lecturer(), scenario.assessment(), scenario.markingItems());

        AppUser student = scenario.student();
        String token = tokenFor(student.getEmail());

        mockMvc.perform(get("/api/feedback/{id}", feedback.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void studentCanReadOwnFeedbackList() throws Exception {
        var scenario = data.scenario();
        data.publishedFeedback(scenario.student(), scenario.lecturer(),
                scenario.assessment(), scenario.markingItems());
        String token = tokenFor(scenario.student().getEmail());

        mockMvc.perform(get("/api/students/{id}/feedback", scenario.student().getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void studentCannotReadAnotherStudentsFeedback() throws Exception {
        var scenario = data.scenario();
        var feedback = data.publishedFeedback(
                scenario.student(), scenario.lecturer(), scenario.assessment(), scenario.markingItems());

        AppUser intruder = data.student();
        String token = tokenFor(intruder.getEmail());

        mockMvc.perform(get("/api/feedback/{id}", feedback.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotReadAnotherStudentsFeedbackList() throws Exception {
        var scenario = data.scenario();
        AppUser intruder = data.student();
        String token = tokenFor(intruder.getEmail());

        mockMvc.perform(get("/api/students/{id}/feedback", scenario.student().getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotReadDraftFeedback() throws Exception {
        var scenario = data.scenario();
        var feedback = data.draftFeedback(
                scenario.student(), scenario.lecturer(), scenario.assessment(), scenario.markingItems());

        AppUser student = scenario.student();
        String token = tokenFor(student.getEmail());

        mockMvc.perform(get("/api/feedback/{id}", feedback.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void lecturerCanReadOwnFeedback() throws Exception {
        var scenario = data.scenario();
        var feedback = data.publishedFeedback(
                scenario.student(), scenario.lecturer(), scenario.assessment(), scenario.markingItems());

        AppUser lecturer = scenario.lecturer();
        String token = tokenFor(lecturer.getEmail());

        mockMvc.perform(get("/api/feedback/{id}", feedback.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void lecturerCannotReadAnotherLecturersFeedback() throws Exception {
        var scenario = data.scenario();
        var feedback = data.publishedFeedback(
                scenario.student(), scenario.lecturer(), scenario.assessment(), scenario.markingItems());
        var scenario2 = data.scenario();

        AppUser intruder = scenario2.lecturer();
        String token = tokenFor(intruder.getEmail());

        mockMvc.perform(get("/api/feedback/{id}", feedback.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotCreateFeedback() throws Exception {
        var scenario = data.scenario();
        String token = tokenFor(scenario.student().getEmail());

        mockMvc.perform(post("/api/feedback")
                        .header("Authorization", "Bearer " + token)
                        .param("publish", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void lecturerCanCreateFeedback() throws Exception {
        var scenario = data.scenario();
        String token = tokenFor(scenario.lecturer().getEmail());

        var request = new CreateFeedbackRequest(
                scenario.assessment().getId(),
                scenario.student().getId(),
                "A summary of the submitted work.",
                List.of(
                        new CreateFeedbackItemRequest(scenario.markingItems().get(0).getId(), (short) 15, "Good."),
                        new CreateFeedbackItemRequest(scenario.markingItems().get(1).getId(), (short) 24, "Fine.")),
                List.of());

        mockMvc.perform(post("/api/feedback")
                        .header("Authorization", "Bearer " + token)
                        .param("publish", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mark").value(39))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void studentCannotReadAnotherStudentsAudio() throws Exception {
        var scenario = data.scenario();
        var feedback = data.publishedFeedback(scenario.student(), scenario.lecturer(),
                scenario.assessment(), scenario.markingItems());

        // lecturer uploads a recording
        var file = new MockMultipartFile("file", "feedback.webm", "audio/webm",
                "fake audio bytes".getBytes());
        mockMvc.perform(multipart("/api/feedback/{id}/audio", feedback.getId())
                        .file(file)
                        .header("Authorization", "Bearer " + tokenFor(scenario.lecturer().getEmail())))
                .andExpect(status().isCreated());

        // an unrelated student tries to fetch it
        AppUser intruder = data.student();
        mockMvc.perform(get("/api/feedback/{id}/audio", feedback.getId())
                        .header("Authorization", "Bearer " + tokenFor(intruder.getEmail())))
                .andExpect(status().isForbidden());
    }

}
