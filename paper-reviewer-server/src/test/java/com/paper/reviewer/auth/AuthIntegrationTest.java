package com.paper.reviewer.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paper.reviewer.auth.security.JsonAuthenticationEntryPoint;
import com.paper.reviewer.auth.security.JwtAuthenticationFilter;
import com.paper.reviewer.auth.security.JwtTokenProvider;
import com.paper.reviewer.auth.service.AuthService;
import com.paper.reviewer.auth.web.AuthController;
import com.paper.reviewer.config.JwtProperties;
import com.paper.reviewer.config.SecurityConfig;
import com.paper.reviewer.common.GlobalExceptionHandler;
import com.paper.reviewer.user.infrastructure.persistence.UserEntity;
import com.paper.reviewer.user.infrastructure.persistence.UserMapper;
import com.paper.reviewer.user.infrastructure.persistence.MyBatisUserRepository;
import org.mybatis.spring.annotation.MapperScan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AuthIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableConfigurationProperties(JwtProperties.class)
    @MapperScan(basePackageClasses = UserMapper.class)
    @Import({AuthController.class, AuthService.class, MyBatisUserRepository.class,
            JwtTokenProvider.class, JwtAuthenticationFilter.class,
            JsonAuthenticationEntryPoint.class, SecurityConfig.class, GlobalExceptionHandler.class})
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUsers() {
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void registerHashesPasswordAndReturnsUsableToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"Researcher@Example.com","password":"password123","displayName":"Researcher"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("researcher@example.com"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        UserEntity stored = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, "researcher@example.com"));
        assertThat(stored.getPasswordHash()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", stored.getPasswordHash())).isTrue();

        String token = objectMapper.readTree(response).path("data").path("token").asText();
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("researcher@example.com"))
                .andExpect(jsonPath("$.data.displayName").value("Researcher"));
    }

    @Test
    void loginAcceptsCorrectPasswordAndRejectsWrongPassword() throws Exception {
        registerUser();

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void protectedApiRejectsMissingAndInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_INVALID"));

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_TOKEN_INVALID"));
    }

    private String registerUser() throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"user@example.com","password":"correct-password","displayName":"User"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}
