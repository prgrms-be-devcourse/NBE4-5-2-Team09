package com.coing.domain.coin.candle.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "upbit_candle")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String type; // 예: "candle.1s"

	@Column(nullable = false)
	private String code; // 예: "KRW-BTC"

	@Column(name = "candle_date_time_utc", nullable = false)
	private LocalDateTime candleDateTimeUtc;

	@Column(name = "candle_date_time_kst", nullable = false)
	private LocalDateTime candleDateTimeKst;

	@Column(nullable = false)
	private double openingPrice;

	@Column(nullable = false)
	private double highPrice;

	@Column(nullable = false)
	private double lowPrice;

	@Column(nullable = false)
	private double tradePrice;

	@Column(nullable = false)
	private double candleAccTradeVolume;

	@Column(nullable = false)
	private double candleAccTradePrice;

	@Column(nullable = false)
	private long timestamp;

	@Column(nullable = false)
	private String streamType;
}
