package com.paper.reviewer.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class)
            .withPropertyValues("app.upload.max-file-size-mb=42", "app.upload.max-page-count=450",
                    "app.upload.max-user-storage-mb=900");

    @Test
    void bindsUploadLimits() {
        contextRunner.run(context -> {
            UploadProperties upload = context.getBean(UploadProperties.class);
            assertThat(upload.maxFileSizeMb()).isEqualTo(42);
            assertThat(upload.maxPageCount()).isEqualTo(450);
            assertThat(upload.maxUserStorageMb()).isEqualTo(900);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(UploadProperties.class)
    static class PropertiesConfiguration { }
}
