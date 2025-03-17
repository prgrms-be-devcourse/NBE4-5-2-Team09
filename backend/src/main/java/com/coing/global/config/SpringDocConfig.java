package com.coing.global.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import com.coing.global.exception.doc.ApiErrorCodeExamples;
import com.coing.global.exception.doc.ErrorCode;
import com.coing.global.exception.doc.ExampleHolder;
import com.coing.util.BasicResponse;
import com.coing.util.MessageUtil;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SpringDoc 설정 클래스.
 * OpenAPI 문서화 및 API 응답 예제를 설정한다.
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@OpenAPIDefinition(info = @Info(title = "Coing API", version = "v0"))
@SecurityScheme(
	name = "bearerAuth",
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT"
)
public class SpringDocConfig {

	private final MessageUtil messageUtil;

	/**
	 * 공개 API 그룹을 정의
	 */
	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder()
			.group("api")
			.pathsToMatch("/api/**")
			.addOperationCustomizer(customize())
			.build();
	}

	/**
	 * API 응답을 커스터마이징하는 메서드
	 * 특정 API에 @ApiErrorCodeExamples 어노테이션이 있는 경우, 예제 응답을 추가
	 */
	@Bean
	public OperationCustomizer customize() {
		return (Operation operation, HandlerMethod handlerMethod) -> {
			// 메서드에서 @ApiErrorCodeExamples 어노테이션을 가져옴
			ApiErrorCodeExamples apiErrorCodeExamples = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);
			if (apiErrorCodeExamples != null) {
				// API 응답에 에러 예제 추가
				addErrorExamplesToResponses(operation.getResponses(), apiErrorCodeExamples.value());
			}
			return operation;
		};
	}

	/**
	 * API 응답에 에러 예제들을 추가하는 메서드
	 */
	private void addErrorExamplesToResponses(ApiResponses responses, ErrorCode[] errorCodes) {
		Map<Integer, List<ExampleHolder>> groupedExamples = Arrays.stream(errorCodes)
			.map(this::createExampleHolder)
			.collect(Collectors.groupingBy(ExampleHolder::statusCode));

		groupedExamples.forEach((status, exampleHolders) -> {
			Content content = new Content();
			MediaType mediaType = new MediaType();
			ApiResponse apiResponse = new ApiResponse();

			// 각 ExampleHolder를 API 응답에 추가
			exampleHolders.forEach(holder -> {
				mediaType.addExamples(holder.name(), holder.example());
			});

			// 응답 형식 설정, api 응답 추가
			content.addMediaType("application/json", mediaType);
			apiResponse.setContent(content);
			responses.addApiResponse(String.valueOf(status), apiResponse);
		});
	}

	/**
	 * 에러 코드에 대한 예제 응답을 생성하는 메서드
	 */
	private ExampleHolder createExampleHolder(ErrorCode errorCode) {
		Example example = new Example();
		example.setValue(BasicResponse.from(errorCode, messageUtil));

		return ExampleHolder.builder()
			.example(example)
			.statusCode(errorCode.getStatus().value())
			.name(errorCode.name())
			.build();
	}
}
