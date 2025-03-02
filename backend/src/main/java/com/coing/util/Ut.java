package com.coing.util;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class Ut {
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
}