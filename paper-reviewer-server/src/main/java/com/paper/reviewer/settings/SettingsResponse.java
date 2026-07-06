package com.paper.reviewer.settings;

public record SettingsResponse(String email, String displayName, String defaultOutputLanguage,
                               String model) { }
