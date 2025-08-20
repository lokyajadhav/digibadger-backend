package com.taashee.badger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import java.time.Duration;


@OpenAPIDefinition(
    info = @Info(
        title = "Badger Management API",
        version = "1.0",
        description = "API documentation for Badger Management system. All endpoints are documented with author: Lokya Naik."
    )
)
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class BadgerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BadgerApplication.class, args);
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("public")
			.pathsToMatch("/**")
			.build();
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		// Configure HTTP client with enterprise-grade settings
		System.out.println("Setting up rest template");
		
		// Use the default RestTemplateBuilder configuration which works with Spring Boot's auto-configuration
		return builder
			.setConnectTimeout(Duration.ofSeconds(30))
			.setReadTimeout(Duration.ofSeconds(60))
			.build();
	}
}
