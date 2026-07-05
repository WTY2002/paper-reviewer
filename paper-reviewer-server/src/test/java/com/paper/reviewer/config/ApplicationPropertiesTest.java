package com.paper.reviewer.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class ApplicationPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class)
            .withPropertyValues(
                    "app.storage.paper-root=/data/papers",
                    "app.storage.export-root=/data/exports",
                    "app.upload.max-file-size-mb=42",
                    "app.upload.max-page-count=450",
                    "app.upload.max-user-storage-mb=900",
                    "app.ai.provider=openai",
                    "app.ai.qwen.api-key=q-key",
                    "app.ai.qwen.model=q-model",
                    "app.ai.openai.enabled=true",
                    "app.ai.openai.api-key=o-key",
                    "app.ai.openai.model=o-model");

    @Test
    void bindsAllCommonInfrastructureProperties() {
        contextRunner.run(context -> {
            StorageProperties storage = context.getBean(StorageProperties.class);
            UploadProperties upload = context.getBean(UploadProperties.class);
            AiModelProperties ai = context.getBean(AiModelProperties.class);

            assertThat(storage.paperRoot()).isEqualTo("/data/papers");
            assertThat(storage.exportRoot()).isEqualTo("/data/exports");
            assertThat(upload.maxFileSizeMb()).isEqualTo(42);
            assertThat(upload.maxPageCount()).isEqualTo(450);
            assertThat(upload.maxUserStorageMb()).isEqualTo(900);
            assertThat(ai.provider()).isEqualTo("openai");
            assertThat(ai.qwen().apiKey()).isEqualTo("q-key");
            assertThat(ai.openai().enabled()).isTrue();
            assertThat(ai.openai().model()).isEqualTo("o-model");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({StorageProperties.class, UploadProperties.class,
            AiModelProperties.class})
    static class PropertiesConfiguration {
    }
}
