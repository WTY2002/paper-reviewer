package com.paper.reviewer.settings;

import com.paper.reviewer.config.AiModelProperties;
import com.paper.reviewer.database.entity.UserEntity;
import com.paper.reviewer.database.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SettingsServiceTest {
    private final UserMapper users = mock(UserMapper.class);
    private final SettingsService service = new SettingsService(users,
            new AiModelProperties("qwen", new AiModelProperties.Qwen("secret", "qwen-plus", "https://example.invalid/v1"), new AiModelProperties.OpenAi(false, "hidden", "gpt")));

    @Test void returnsModelStatusWithoutAnyApiKey() {
        UserEntity user = user(); when(users.selectById(1L)).thenReturn(user);
        SettingsResponse result = service.get(1L);
        assertThat(result.provider()).isEqualTo("qwen"); assertThat(result.openAiEnabled()).isFalse();
        assertThat(result.toString()).doesNotContain("secret", "hidden");
    }

    @Test void updatesAllowedPreferences() {
        UserEntity user = user(); when(users.selectById(1L)).thenReturn(user);
        SettingsResponse result = service.update(1L, new UpdateSettingsRequest("Ada", "zh"));
        assertThat(result.displayName()).isEqualTo("Ada"); assertThat(result.defaultOutputLanguage()).isEqualTo("zh"); verify(users).updateById(user);
    }

    @Test void rejectsUnsupportedLanguage() {
        when(users.selectById(1L)).thenReturn(user());
        assertThatThrownBy(() -> service.update(1L, new UpdateSettingsRequest(null, "fr"))).isInstanceOf(IllegalArgumentException.class);
    }

    private UserEntity user() { UserEntity user = new UserEntity(); user.setId(1L); user.setEmail("ada@example.com"); user.setDisplayName("A"); return user; }
}
