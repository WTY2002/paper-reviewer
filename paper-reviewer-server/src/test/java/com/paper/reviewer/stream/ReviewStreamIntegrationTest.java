package com.paper.reviewer.stream;

import com.paper.reviewer.review.infrastructure.persistence.ReviewEntity;
import com.paper.reviewer.review.infrastructure.persistence.ReviewMapper;
import com.paper.reviewer.stream.domain.ReviewEvent;
import com.paper.reviewer.stream.domain.ReviewEventType;
import com.paper.reviewer.stream.service.ReviewEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewStreamIntegrationTest {
    private static final long REVIEW_ID = 88001L;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ReviewMapper reviewMapper;
    @Autowired ReviewEventService eventService;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanFixture() {
        jdbcTemplate.update("DELETE FROM review_events WHERE review_id = ?", REVIEW_ID);
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", REVIEW_ID);
        jdbcTemplate.update("DELETE FROM users WHERE email IN (?, ?)", "stream-owner@example.com", "stream-other@example.com");
    }

    @Test
    void persistsEventsWithAtomicSequenceAndOrderedHistory() throws Exception {
        long ownerId = register("stream-owner@example.com").userId();
        insertReview(ownerId);

        int eventCount = 12;
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<ReviewEvent>> tasks = new ArrayList<>();
            for (int i = 0; i < eventCount; i++) {
                int index = i;
                tasks.add(() -> eventService.publish(REVIEW_ID, ReviewEventType.REVIEWER_REPORT_STARTED,
                        "REVIEWING", "EIC", objectMapper.createObjectNode().put("index", index)));
            }
            List<Future<ReviewEvent>> futures = executor.invokeAll(tasks);
            for (Future<ReviewEvent> future : futures) future.get();
        } finally {
            executor.shutdownNow();
        }

        List<ReviewEvent> history = eventService.history(REVIEW_ID);
        assertThat(history).hasSize(eventCount);
        assertThat(history).extracting(ReviewEvent::sequence)
                .containsExactlyElementsOf(java.util.stream.LongStream.rangeClosed(1, eventCount).boxed().toList());
        assertThat(history).allSatisfy(event -> {
            assertThat(event.type()).isEqualTo(ReviewEventType.REVIEWER_REPORT_STARTED);
            assertThat(event.payload().path("index").isInt()).isTrue();
        });
    }

    @Test
    void requiresJwtAndOnlyAllowsOwnerWhileReplayingHistory() throws Exception {
        Registration owner = register("stream-owner@example.com");
        Registration other = register("stream-other@example.com");
        insertReview(owner.userId());
        eventService.publishFailure(REVIEW_ID, "REVIEWING", "provider unavailable");

        mockMvc.perform(get("/api/reviews/{id}/stream", REVIEW_ID))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reviews/{id}/stream", REVIEW_ID)
                        .header("Authorization", "Bearer " + other.token()))
                .andExpect(status().isNotFound());

        MvcResult result = mockMvc.perform(get("/api/reviews/{id}/stream", REVIEW_ID)
                        .header("Authorization", "Bearer " + owner.token())
                        .accept("text/event-stream"))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        String stream = result.getResponse().getContentAsString();
        assertThat(stream).contains("event:REVIEW_EVENT", "id:1", "REVIEW_FAILED", "provider unavailable");
    }

    private Registration register(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\",\"displayName\":\"Stream User\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(response).path("data");
        return new Registration(data.path("userId").asLong(), data.path("token").asText());
    }

    private void insertReview(long ownerId) {
        jdbcTemplate.update("INSERT INTO papers (id,user_id,title,original_filename,file_path,file_size,page_count,status,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                777L, ownerId, "Stream paper", "paper.pdf", "test.pdf", 1L, 1, "EXTRACTED");
        ReviewEntity review = new ReviewEntity();
        review.setId(REVIEW_ID);
        review.setUserId(ownerId);
        review.setPaperId(777L);
        review.setReviewType("FULL");
        review.setStatus("REVIEWING");
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
    }

    private record Registration(long userId, String token) {}
}
