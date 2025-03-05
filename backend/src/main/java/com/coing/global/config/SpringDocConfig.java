package com.coing.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Coing API", version = "v0"))
@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT"
)
public class SpringDocConfig {

	@Bean
	public GroupedOpenApi groupApi() {
		return GroupedOpenApi.builder()
			.group("api")
			.pathsToMatch("/api/**")
			.build();
	}

	@Bean
	public GroupedOpenApi groupController() {
		return GroupedOpenApi.builder()
			.group("controller")
			.pathsToExclude("/api/**")
			.build();
	}
}
