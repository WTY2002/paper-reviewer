package com.paper.reviewer.auth.service;

public record RegisterCommand(String email, String password, String displayName) { }
