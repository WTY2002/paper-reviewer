package com.paper.reviewer.review.dto;

import com.paper.reviewer.review.domain.ReviewType;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRequest(@NotNull Long paperId, @NotNull ReviewType reviewType,
                                  String outputLanguage) { }
