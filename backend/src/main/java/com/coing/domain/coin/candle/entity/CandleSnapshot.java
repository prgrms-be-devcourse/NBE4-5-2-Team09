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
@Table(name = "candle_snapshot")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandleSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 캔들 타입
	@Column(nullable = false)
	private String type;

	// 마켓 코드 (예: KRW-BTC)
	@Column(nullable = false)
	private String code;

	@Column(name = "candle_date_time_utc", nullable = false)
	private String candleDateTimeUtc;

	@Column(name = "candle_date_time_kst", nullable = false)
	private String candleDateTimeKst;

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

	// 업비트에서 제공하는 타임스탬프 (체결 시각 또는 전송 시각)
	@Column(nullable = false)
	private long timestamp;

	// 스트림 타입 (SNAPSHOT 또는 REALTIME)
	@Column(nullable = false)
	private String streamType;

	// 스냅샷이 DB에 저장된 시각 (저장 시각)
	@Column(nullable = false)
	private LocalDateTime snapshotTimestamp;
}