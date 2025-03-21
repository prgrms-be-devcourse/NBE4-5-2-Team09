package com.coing.global.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.coing.domain.user.service.OAuth2UserService;
import com.coing.global.security.CustomAuthenticationEntryPoint;
import com.coing.global.security.filter.JwtAuthenticationFilter;
import com.coing.global.security.handler.OAuth2LoginFailureHandler;
import com.coing.global.security.handler.OAuth2LoginSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final OAuth2UserService oAuth2UserService;
	private final OAuth2LoginSuccessHandler successHandler;
	private final OAuth2LoginFailureHandler failureHandler;

	@Value("${next.server.url}")
	private String frontUrl;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CORS 설정 추가
			.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
			// CSRF 보호 비활성화
			.csrf(AbstractHttpConfigurer::disable)
			// H2 콘솔 접근을 허용하기 위해 frameOptions 비활성화
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			// 세션을 사용하지 않는 stateless 설정
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// 모든 요청에 대해 permitAll()
			.authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
			// 커스텀 AuthenticationEntryPoint 및 JwtAuthenticationFilter 추가
			.exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint))
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			// OAuth2 로그인 관련 설정
			.oauth2Login((oauth2) -> oauth2
				.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
					.userService(oAuth2UserService))
				.successHandler(successHandler)
				.failureHandler(failureHandler));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// 모든 출처 허용 (운영 환경 배포 시 수정 필요)
		configuration.setAllowedOriginPatterns(Arrays.asList(frontUrl));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.addExposedHeader("Authorization");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// 모든 엔드포인트에 대해 CORS 설정 적용
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// 매개변수: saltLength, hashLength, parallelism, memory, iterations
		return new Argon2PasswordEncoder(16, 32, 1, 4096, 3);
	}
}