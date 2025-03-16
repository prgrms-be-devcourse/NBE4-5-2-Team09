package com.coing.domain.user.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.coing.domain.user.dto.CustomUserPrincipal;
import com.coing.domain.user.controller.dto.UserResponse;
import com.coing.util.Ut;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenService {

	@Value("${custom.jwt.secret-key}")
	private String jwtSecretKey;

	@Value("${custom.jwt.expire-seconds}")
	private int jwtExpireSeconds;

	@Value("${custom.jwt.refresh-expire-seconds}") // 예: 7일 (초 단위)
	private int jwtRefreshExpireSeconds;

	// 액세스 토큰 생성: UserResponse를 사용
	public String genAccessToken(UserResponse userResponse) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", userResponse.id());
		// claims.put("authority", userResponse.getAuthority()); // 나중에 권한 관련 추가
		String token = Ut.Jwt.createToken(jwtSecretKey, jwtExpireSeconds, claims);
		log.info("JWT 액세스 토큰 생성: {}", userResponse.email());
		return token;
	}

	// 리프레시 토큰 생성: UserResponse를 사용
	public String genRefreshToken(UserResponse userResponse) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", userResponse.id());
		String token = Ut.Jwt.createToken(jwtSecretKey, jwtRefreshExpireSeconds, claims);
		log.info("JWT 리프레시 토큰 생성: {}", userResponse.email());
		return token;
	}

	// 액세스 토큰 생성: CustomUserPrincipal을 사용
	public String genAccessToken(CustomUserPrincipal principal) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", principal.id());
		String token = Ut.Jwt.createToken(jwtSecretKey, jwtExpireSeconds, claims);
		log.info("JWT 액세스 토큰 생성(CustomUserPrincipal): {}", principal.id());
		return token;
	}

	// 리프레시 토큰 생성: CustomUserPrincipal을 사용
	public String genRefreshToken(CustomUserPrincipal principal) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", principal.id());
		String token = Ut.Jwt.createToken(jwtSecretKey, jwtRefreshExpireSeconds, claims);
		log.info("JWT 리프레시 토큰 생성(CustomUserPrincipal): {}", principal.id());
		return token;
	}

	public Map<String, Object> verifyToken(String token) {
		try {
			// Ut.Jwt.verifyToken() 메서드를 사용하여 토큰을 검증하고 클레임을 반환한다고 가정합니다.
			return Ut.verifyToken(jwtSecretKey, token);
		} catch (Exception e) {
			log.error("토큰 검증 실패", e);
			return null;
		}
	}

	public UUID parseId(String token) {
		Map<String, Object> claims = verifyToken(token);
		if (claims == null || claims.get("id") == null) {
			return null;
		}
		return UUID.fromString(claims.get("id").toString());
	}
}
