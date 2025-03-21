package com.coing.infra.upbit.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.coing.domain.coin.orderbook.entity.Orderbook;
import com.coing.domain.coin.orderbook.entity.OrderbookUnit;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Upbit WebSocket Orderbook(호가) Response Dto
 * format field : "SIMPLE"로 지정하여 응답의 필드명이 모두 간소화함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitWebSocketOrderbookDto {
	@JsonProperty("ty")
	private String type;

	@JsonProperty("cd")
	private String code;

	@JsonProperty("tas")
	private Double totalAskSize;

	@JsonProperty("tbs")
	private Double totalBidSize;

	@JsonProperty("obu")
	private List<OrderbookUnitDto> orderbookUnits;

	@JsonProperty("tms")
	private Long timestamp;

	@JsonProperty("lv")
	private Double level;

	@JsonProperty("st")
	private String streamType;

	public Orderbook toEntity() {
		Orderbook.OrderbookBuilder orderbookBuilder = Orderbook.builder()
			.type(this.type)
			.code(this.code)
			.totalAskSize(this.totalAskSize)
			.totalBidSize(this.totalBidSize)
			.timestamp(this.timestamp)
			.level(this.level);

		if (this.orderbookUnits != null) {
			List<OrderbookUnit> units = this.orderbookUnits.stream()
				.map(OrderbookUnitDto::toEntity)
				.collect(Collectors.toList());
			orderbookBuilder.orderbookUnits(units);
		}

		return orderbookBuilder.build();
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OrderbookUnitDto {
		@JsonProperty("ap")
		private Double askPrice;

		@JsonProperty("bp")
		private Double bidPrice;

		@JsonProperty("as")
		private Double askSize;

		@JsonProperty("bs")
		private Double bidSize;

		public OrderbookUnit toEntity() {
			return new OrderbookUnit(askPrice, bidPrice, askSize, bidSize);
		}
	}

}
