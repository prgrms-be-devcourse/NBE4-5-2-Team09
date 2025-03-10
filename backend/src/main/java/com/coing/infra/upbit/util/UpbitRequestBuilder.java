package com.coing.infra.upbit.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.service.MarketCacheService;
import com.coing.infra.upbit.dto.UpbitWebSocketFormatDto;
import com.coing.infra.upbit.dto.UpbitWebSocketTicketDto;
import com.coing.infra.upbit.dto.UpbitWebSocketTypeDto;
import com.coing.infra.upbit.enums.EnumUpbitRequestType;
import com.coing.infra.upbit.enums.EnumUpbitWebSocketFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpbitRequestBuilder {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final MarketCacheService marketCacheService;

	public String makeRequest(EnumUpbitRequestType type) throws JsonProcessingException {
		List<String> codes = marketCacheService.getCachedMarketList().stream().map(Market::getCode).toList();
		UpbitWebSocketTicketDto ticketDto = UpbitWebSocketTicketDto.builder()
			.ticket(type.getValue())
			.build();
		UpbitWebSocketTypeDto typeDto = UpbitWebSocketTypeDto.builder()
			.type(type.getValue())
			.codes(codes)
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
