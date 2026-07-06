package com.paper.reviewer.reviewerteam.infrastructure.persistence;

import com.paper.reviewer.reviewerteam.repository.ReviewerTeamRepository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.reviewerteam.infrastructure.persistence.ReviewerTeamEntity;
import com.paper.reviewer.reviewerteam.infrastructure.persistence.ReviewerTeamMapper;
import com.paper.reviewer.reviewerteam.domain.Reviewer;
import com.paper.reviewer.reviewerteam.domain.ReviewerRole;
import com.paper.reviewer.reviewerteam.domain.ReviewerTeam;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisReviewerTeamRepository implements ReviewerTeamRepository {
    private final ReviewerTeamMapper mapper;
    private final ObjectMapper objectMapper;

    public MyBatisReviewerTeamRepository(ReviewerTeamMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public ReviewerTeam save(ReviewerTeam team) {
        ReviewerTeamEntity entity = team.id() == null ? new ReviewerTeamEntity() : mapper.selectById(team.id());
        if (entity == null) throw new IllegalStateException("Reviewer team no longer exists");
        entity.setReviewId(team.reviewId());
        entity.setTargetVenue(team.targetVenue());
        entity.setTeamJson(toJson(team.reviewers()));
        entity.setConfirmedAt(team.confirmedAt());
        entity.setCreatedAt(team.createdAt());
        entity.setUpdatedAt(team.updatedAt());
        if (team.id() == null) mapper.insert(entity); else mapper.updateById(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<ReviewerTeam> findByReviewId(long reviewId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<ReviewerTeamEntity>()
                .eq(ReviewerTeamEntity::getReviewId, reviewId))).map(this::toDomain);
    }

    private JsonNode toJson(List<Reviewer> reviewers) {
        ArrayNode array = objectMapper.createArrayNode();
        for (Reviewer reviewer : reviewers) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("role", reviewer.role().name());
            node.put("displayName", reviewer.displayName());
            node.put("identityDescription", reviewer.identityDescription());
            node.put("expertise", reviewer.expertise());
            node.put("reviewFocus", reviewer.reviewFocus());
            array.add(node);
        }
        return objectMapper.createObjectNode().set("reviewers", array);
    }

    private ReviewerTeam toDomain(ReviewerTeamEntity entity) {
        JsonNode root = entity.getTeamJson();
        JsonNode array = root == null ? null : root.get("reviewers");
        if (array == null || !array.isArray()) throw new IllegalStateException("Invalid reviewer team JSON");
        List<Reviewer> reviewers = new ArrayList<>();
        for (JsonNode node : array) {
            reviewers.add(new Reviewer(
                    ReviewerRole.valueOf(requiredText(node, "role")),
                    requiredText(node, "displayName"),
                    requiredText(node, "identityDescription"),
                    requiredText(node, "expertise"),
                    requiredText(node, "reviewFocus")));
        }
        return new ReviewerTeam(entity.getId(), entity.getReviewId(), entity.getTargetVenue(), reviewers,
                entity.getConfirmedAt(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private String requiredText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual()) throw new IllegalStateException("Invalid reviewer field: " + field);
        return value.asText();
    }
}
