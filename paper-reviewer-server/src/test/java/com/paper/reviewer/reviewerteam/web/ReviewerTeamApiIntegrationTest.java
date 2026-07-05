package com.paper.reviewer.reviewerteam.web;

import com.paper.reviewer.database.entity.ReviewEntity;
import com.paper.reviewer.database.mapper.ReviewMapper;
import com.paper.reviewer.reviewerteam.domain.Reviewer;
import com.paper.reviewer.reviewerteam.domain.ReviewerRole;
import com.paper.reviewer.reviewerteam.domain.ReviewerTeam;
import com.paper.reviewer.reviewerteam.repository.ReviewerTeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewerTeamApiIntegrationTest {
    private static final long REVIEW_ID = 89001L;
    private static final String EMAIL = "reviewer-team-owner@example.com";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ReviewMapper reviewMapper;
    @Autowired ReviewerTeamRepository teamRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    private long userId;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.update("DELETE FROM review_events WHERE review_id = ?", REVIEW_ID);
        jdbcTemplate.update("DELETE FROM reviewer_teams WHERE review_id = ?", REVIEW_ID);
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", REVIEW_ID);
        jdbcTemplate.update("DELETE FROM users WHERE email = ?", EMAIL);
        JsonNode registration = objectMapper.readTree(mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL
                                + "\",\"password\":\"password123\",\"displayName\":\"Team Owner\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("data");
        userId = registration.path("userId").asLong();
        token = registration.path("token").asText();
        insertReview();
        LocalDateTime now = LocalDateTime.now();
        teamRepository.save(new ReviewerTeam(null, REVIEW_ID, "Initial Venue", reviewers(), null, now, now));
    }

    @Test
    void getsEditsAndConfirmsTeamThenLocksIt() throws Exception {
        mockMvc.perform(get("/api/reviews/{id}/team", REVIEW_ID).header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TEAM_PENDING"))
                .andExpect(jsonPath("$.data.reviewers.length()").value(5));

        ObjectNode update = updatePayload("Edited Venue");
        update.withArray("reviewers").forEach(node -> {
            ((ObjectNode) node).put("identityDescription", "edited " + node.path("role").asText());
            ((ObjectNode) node).put("reviewFocus", "focused " + node.path("role").asText());
        });
        mockMvc.perform(put("/api/reviews/{id}/team", REVIEW_ID).header("Authorization", bearer())
                        .contentType("application/json").content(update.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.targetVenue").value("Edited Venue"));

        mockMvc.perform(post("/api/reviews/{id}/confirm-team", REVIEW_ID).header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("TEAM_CONFIRMED"));

        assertThat(reviewMapper.selectById(REVIEW_ID).getStatus()).isEqualTo("TEAM_CONFIRMED");
        assertThat(teamRepository.findByReviewId(REVIEW_ID).orElseThrow().confirmedAt()).isNotNull();
        mockMvc.perform(put("/api/reviews/{id}/team", REVIEW_ID).header("Authorization", bearer())
                        .contentType("application/json").content(update.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("REVIEW_INVALID_STATUS"));
    }

    @Test
    void rejectsRoleMutationAndReviewerRemoval() throws Exception {
        ObjectNode changedRole = updatePayload("Venue");
        ((ObjectNode) changedRole.withArray("reviewers").get(0)).put("role", "DOMAIN");
        mockMvc.perform(put("/api/reviews/{id}/team", REVIEW_ID).header("Authorization", bearer())
                        .contentType("application/json").content(changedRole.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("REQUEST_VALIDATION_FAILED"));

        ObjectNode removed = updatePayload("Venue");
        removed.withArray("reviewers").remove(0);
        mockMvc.perform(put("/api/reviews/{id}/team", REVIEW_ID).header("Authorization", bearer())
                        .contentType("application/json").content(removed.toString()))
                .andExpect(status().isBadRequest());
    }

    private ObjectNode updatePayload(String venue) {
        ObjectNode root = objectMapper.createObjectNode().put("targetVenue", venue);
        ArrayNode array = root.putArray("reviewers");
        reviewers().forEach(reviewer -> array.add(objectMapper.createObjectNode()
                .put("role", reviewer.role().name())
                .put("displayName", reviewer.displayName())
                .put("identityDescription", reviewer.identityDescription())
                .put("expertise", reviewer.expertise())
                .put("reviewFocus", reviewer.reviewFocus())));
        return root;
    }

    private List<Reviewer> reviewers() {
        return List.of(ReviewerRole.values()).stream().map(role -> new Reviewer(role, role + " name",
                role + " identity", role + " expertise", role + " focus")).toList();
    }

    private void insertReview() {
        jdbcTemplate.update("INSERT INTO papers (id,user_id,title,original_filename,file_path,file_size,page_count,status,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                999L, userId, "Team paper", "paper.pdf", "test.pdf", 1L, 1, "EXTRACTED");
        ReviewEntity review = new ReviewEntity();
        review.setId(REVIEW_ID);
        review.setUserId(userId);
        review.setPaperId(999L);
        review.setReviewType("FULL");
        review.setStatus("TEAM_PENDING");
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    private String bearer() { return "Bearer " + token; }
}
