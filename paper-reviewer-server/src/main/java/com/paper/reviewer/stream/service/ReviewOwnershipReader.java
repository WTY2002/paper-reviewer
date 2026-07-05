package com.paper.reviewer.stream.service;

/** Minimal boundary needed by streaming; the review module need not be exposed here. */
public interface ReviewOwnershipReader {
    boolean isOwnedBy(long reviewId, long userId);
}
