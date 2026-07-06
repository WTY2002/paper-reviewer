package com.paper.reviewer.settings;

import com.paper.reviewer.user.infrastructure.persistence.UserEntity;
import com.paper.reviewer.user.infrastructure.persistence.UserMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SettingsServiceTest {
    private final UserMapper users = mock(UserMapper.class);
    private final SettingsService service = new SettingsService(users, "qwen-plus");

    @Test void returnsConfiguredModel() {
        UserEntity user = user(); when(users.selectById(1L)).thenReturn(user);
        SettingsResponse result = service.get(1L);
        assertThat(result.model()).isEqualTo("qwen-plus");
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
