package com.coing.domain.coin.candle.entity;

import com.coing.domain.coin.candle.dto.CandleDto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CandleAggregator {
	private final String code;      // 마켓 코드 (예: "KRW-BTC")
	private final long startTime;   // 초 단위 시작 시각 (ex: 1678345600000 -> 2023-03-09 12:00:00 UTC)
	private double open;            // 시가
	private double high;            // 고가
	private double low;             // 저가
	private double close;           // 종가
	private double volume;          // 거래량 (단순 누적)

	public CandleAggregator(String code, long startTime, double firstPrice) {
		this.code = code;
		this.startTime = startTime;
		this.open = firstPrice;
		this.high = firstPrice;
		this.low = firstPrice;
		this.close = firstPrice;
		this.volume = 0.0;
	}

	/**
	 * 새 이벤트(체결 데이터 등)를 사용해 현재 봉을 업데이트합니다.
	 */
	public void update(double tradePrice, double tradeVolume) {
		if (tradePrice > high)
			high = tradePrice;
		if (tradePrice < low)
			low = tradePrice;
		close = tradePrice;
		volume += tradeVolume;
	}

	/**
	 * 웹소켓으로 전송할 DTO 등으로 변환하는 예시
	 */
	public CandleDto toCandleDto() {
		CandleDto dto = new CandleDto();
		dto.setCode(code);
		dto.setTimestamp(startTime); // 1초봉 시작 시각(또는 끝 시각)으로 설정
		dto.setOpen(open);
		dto.setHigh(high);
		dto.setLow(low);
		dto.setClose(close);
		dto.setVolume(volume);
		return dto;
	}
}
