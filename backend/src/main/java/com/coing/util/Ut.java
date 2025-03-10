package com.coing.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class Ut {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static class Jwt {
		public static String createToken(String keyString, int expireSeconds, Map<String, Object> claims) {

			SecretKey secretKey = Keys.hmacShaKeyFor(keyString.getBytes());

			Date issuedAt = new Date();
			Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

			String jwt = Jwts.builder()
				.claims(claims)
				.issuedAt(issuedAt)
				.expiration(expiration)
				.signWith(secretKey)
				.compact();

			return jwt;
		}

		public static boolean isValidToken(String keyString, String token) {
			try {

				SecretKey secretKey = Keys.hmacShaKeyFor(keyString.getBytes());

				Jwts
					.parser()
					.verifyWith(secretKey)
					.build()
					.parse(token);

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			return true;

		}

		public static Map<String, Object> getPayload(String keyString, String jwtStr) {

			SecretKey secretKey = Keys.hmacShaKeyFor(keyString.getBytes());

			return (Map<String, Object>)Jwts
				.parser()
				.verifyWith(secretKey)
				.build()
				.parse(jwtStr)
				.getPayload();

		}
	}

	public static class Json {

		public static String toString(Object obj) {
			try {
				return objectMapper.writeValueAsString(obj);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static Map<String, Object> verifyToken(String secretKey, String token) {
		try {
			SecretKey sKey = Keys.hmacShaKeyFor(secretKey.getBytes());
			Claims claims = Jwts.parser()
				.setSigningKey(sKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
			return new HashMap<>(claims);
		} catch (JwtException e) {
			// 토큰 검증 실패
			return null;
		}
	}

	public class AuthTokenUtil {
		// 메일 인증용 JWT를 생성하는 유틸  (만료 10분)
		public static String createEmailVerificationToken(String secretKey, UUID userId) {
			Map<String, Object> claims = new HashMap<>();
			claims.put("id", userId.toString());
			// 10분 만료 (600초)
			int expireSeconds = 600;
			return Ut.Jwt.createToken(secretKey, expireSeconds, claims);
		}
	}
}