package com.paper.reviewer.reviewerteam.service;

import com.paper.reviewer.reviewerteam.domain.ReviewerTeam;

public interface ReviewerTeamEventPublisher {
    void generated(ReviewerTeam team);
    void confirmed(ReviewerTeam team);
}
