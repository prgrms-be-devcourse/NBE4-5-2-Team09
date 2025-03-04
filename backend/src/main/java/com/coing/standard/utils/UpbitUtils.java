package com.coing.standard.utils;

import java.util.Arrays;
import java.util.List;

import com.coing.infra.upbit.dto.UpbitWebSocketFormatDto;
import com.coing.infra.upbit.dto.UpbitWebSocketTicketDto;
import com.coing.infra.upbit.dto.UpbitWebSocketTypeDto;
import com.coing.infra.upbit.enums.EnumUpbitRequestType;
import com.coing.infra.upbit.enums.EnumUpbitWebSocketFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Upbit WebSocket Request Message를 생성하는 Utility Class
 * <p>
 * 티켓(ticket), 타입(type), 포맷(format) 정보를 포함한 JSON 배열을 SIMPLE 포맷으로 반환
 */
public class UpbitUtils {
	private final static ObjectMapper ObjectMapper = new ObjectMapper();

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
		return ObjectMapper.writeValueAsString(dataList);
	}
}
