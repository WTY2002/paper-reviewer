package com.paper.reviewer.database;

import com.paper.reviewer.review.infrastructure.persistence.ReviewEntity;
import com.paper.reviewer.user.infrastructure.persistence.UserEntity;
import com.paper.reviewer.review.infrastructure.persistence.ReviewMapper;
import com.paper.reviewer.user.infrastructure.persistence.UserMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DatabasePersistenceTests.TestApplication.class)
@Transactional
class DatabasePersistenceTests {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan("com.paper.reviewer")
    static class TestApplication {
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void schemaCreatesAllNineTables() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('users', 'papers', 'paper_extractions', 'reviews',
                    'reviewer_teams', 'review_reports', 'review_events', 'rereviews', 'exports')
                """, Integer.class);

        assertThat(count).isEqualTo(9);
    }

    @Test
    void repositoryInsertsReadsAndPhysicallyDeletes() {
        UserEntity user = new UserEntity();
        user.setEmail("researcher@example.com");
        user.setPasswordHash("hash");
        user.setDisplayName("Researcher");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        assertThat(userMapper.insert(user)).isEqualTo(1);
        assertThat(user.getId()).isNotNull();
        assertThat(userMapper.selectById(user.getId()).getEmail()).isEqualTo(user.getEmail());

        assertThat(userMapper.deleteById(user.getId())).isEqualTo(1);
        assertThat(userMapper.selectById(user.getId())).isNull();
        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, user.getId());
        assertThat(remaining).isZero();
    }

    @Test
    void jsonTypeHandlerRoundTripsJsonNode() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("json@example.com");
        user.setPasswordHash("hash");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        jdbcTemplate.update("INSERT INTO papers (id,user_id,title,original_filename,file_path,file_size,page_count,status,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                42L, user.getId(), "JSON paper", "paper.pdf", "test.pdf", 1L, 1, "EXTRACTED");

        ReviewEntity review = new ReviewEntity();
        review.setUserId(user.getId());
        review.setPaperId(42L);
        review.setReviewType("FULL");
        review.setStatus("ANALYZING");
        review.setFieldAnalysisJson(objectMapper.readTree("{\"field\":\"computer science\",\"confidence\":0.9}"));
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        reviewMapper.insert(review);
        ReviewEntity reloaded = reviewMapper.selectById(review.getId());

        assertThat(reloaded.getFieldAnalysisJson().path("field").asText()).isEqualTo("computer science");
        assertThat(reloaded.getFieldAnalysisJson().path("confidence").asDouble()).isEqualTo(0.9);
    }
}
