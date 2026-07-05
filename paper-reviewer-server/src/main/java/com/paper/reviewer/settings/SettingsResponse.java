package com.paper.reviewer.settings;

public record SettingsResponse(String email, String displayName, String defaultOutputLanguage,
                               String provider, String model, boolean openAiEnabled) { }
