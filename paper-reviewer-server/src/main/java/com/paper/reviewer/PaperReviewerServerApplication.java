package com.paper.reviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PaperReviewerServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperReviewerServerApplication.class, args);
    }

}
