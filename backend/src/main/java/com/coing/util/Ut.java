package com.coing.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.coing.infra.upbit.dto.UpbitWebSocketFormatDto;
import com.coing.infra.upbit.dto.UpbitWebSocketTicketDto;
import com.coing.infra.upbit.dto.UpbitWebSocketTypeDto;
import com.coing.infra.upbit.enums.EnumUpbitRequestType;
import com.coing.infra.upbit.enums.EnumUpbitWebSocketFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public static class Upbit {

		public static String makeRequest(EnumUpbitRequestType type) throws JsonProcessingException {
			UpbitWebSocketTicketDto ticketDto = UpbitWebSocketTicketDto.builder()
				.ticket(type.getValue())
				.build();
			UpbitWebSocketTypeDto typeDto = UpbitWebSocketTypeDto.builder()
				.type(type.getValue())
				.codes(type.getDefaultCodes())
				.isOnlyRealtime(false)
				.isOnlySnapshot(false)
				.build();
			UpbitWebSocketFormatDto formatDto = UpbitWebSocketFormatDto.builder()
				.format(EnumUpbitWebSocketFormat.SIMPLE)
				.build();

			List<Object> dataList = Arrays.asList(ticketDto, typeDto, formatDto);
			return objectMapper.writeValueAsString(dataList);
		}
	}
}