package com.paper.reviewer.settings;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.config.AiModelProperties;
import com.paper.reviewer.database.entity.UserEntity;
import com.paper.reviewer.database.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
public class SettingsService {
    private static final Set<String> LANGUAGES = Set.of("AUTO", "zh", "en");
    private final UserMapper users;
    private final AiModelProperties ai;

    public SettingsService(UserMapper users, AiModelProperties ai) { this.users = users; this.ai = ai; }

    public SettingsResponse get(long userId) { return response(requiredUser(userId)); }

    public SettingsResponse update(long userId, UpdateSettingsRequest request) {
        UserEntity user = requiredUser(userId);
        if (request.displayName() != null) {
            String displayName = request.displayName().trim();
            if (displayName.isEmpty() || displayName.length() > 100) throw new IllegalArgumentException("Display name must be 1-100 characters");
            user.setDisplayName(displayName);
        }
        if (request.defaultOutputLanguage() != null) {
            String language = normalizeLanguage(request.defaultOutputLanguage());
            if (!LANGUAGES.contains(language)) throw new IllegalArgumentException("Output language must be AUTO, zh, or en");
            user.setDefaultOutputLanguage(language);
        }
        user.setUpdatedAt(LocalDateTime.now()); users.updateById(user); return response(user);
    }

    private UserEntity requiredUser(long userId) {
        UserEntity user = users.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.AUTH_TOKEN_INVALID);
        return user;
    }

    private String normalizeLanguage(String value) { return value.equalsIgnoreCase("auto") ? "AUTO" : value.toLowerCase(Locale.ROOT); }
    private SettingsResponse response(UserEntity user) {
        return new SettingsResponse(user.getEmail(), user.getDisplayName(), user.getDefaultOutputLanguage() == null ? "AUTO" : user.getDefaultOutputLanguage(),
                ai.provider() == null ? "qwen" : ai.provider(), ai.qwen() == null ? null : ai.qwen().model(), ai.openai() != null && ai.openai().enabled());
    }
}
