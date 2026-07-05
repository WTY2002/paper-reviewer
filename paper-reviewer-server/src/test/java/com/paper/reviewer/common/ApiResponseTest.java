package com.paper.reviewer.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class ApiResponseTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void successResponseUsesStableEnvelope() throws Exception {
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(ApiResponse.success("ok")));

        assertThat(json.get("success").asBoolean()).isTrue();
        assertThat(json.get("data").asText()).isEqualTo("ok");
        assertThat(json.get("error").isNull()).isTrue();
    }

    @Test
    void errorResponseUsesStableCodeAndMessage() throws Exception {
        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.PAPER_FILE_TOO_LARGE, "PDF file size exceeds 20MB");
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("success").asBoolean()).isFalse();
        assertThat(json.get("data").isNull()).isTrue();
        assertThat(json.at("/error/code").asText()).isEqualTo("PAPER_FILE_TOO_LARGE");
        assertThat(json.at("/error/message").asText()).isEqualTo("PDF file size exceeds 20MB");
    }
}
