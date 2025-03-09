package com.coing.domain.coin.candle.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.candle.dto.CandleDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpbitCandleService {

	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * 특정 마켓과 캔들 타입에 따른 보정된 캔들 데이터를 Upbit REST API로부터 가져옵니다.
	 * 분봉의 경우 unit 파라미터를 사용합니다.
	 */
	public List<CandleDto> getLatestCandles(String market, String candleType, Integer unit) {
		String url = "";
		switch (candleType.toLowerCase()) {
			case "seconds":
				url = "https://api.upbit.com/v1/candles/seconds?market=" + market + "&count=200";
				break;
			case "minutes":
				// unit이 null이면 기본값 1로 처리
				int minuteUnit = (unit != null) ? unit : 1;
				url = "https://api.upbit.com/v1/candles/minutes/" + minuteUnit + "?market=" + market + "&count=200";
				break;
			case "days":
				url = "https://api.upbit.com/v1/candles/days?market=" + market + "&count=200";
				break;
			case "weeks":
				url = "https://api.upbit.com/v1/candles/weeks?market=" + market + "&count=200";
				break;
			case "months":
				url = "https://api.upbit.com/v1/candles/months?market=" + market + "&count=200";
				break;
			case "years":
				url = "https://api.upbit.com/v1/candles/years?market=" + market + "&count=200";
				break;
			default:
				log.warn("알 수 없는 캔들 타입: {}", candleType);
				return Collections.emptyList();
		}

		try {
			ResponseEntity<CandleDto[]> response = restTemplate.getForEntity(url, CandleDto[].class);
			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				CandleDto[] candles = response.getBody();
				// API는 최신 캔들을 내림차순으로 반환할 수 있으므로, 오름차순 정렬
				List<CandleDto> candleList = Arrays.asList(candles);
				Collections.reverse(candleList);
				return candleList;
			} else {
				log.warn("Upbit candle API 호출 실패: {}", response.getStatusCode());
				return Collections.emptyList();
			}
		} catch (Exception e) {
			log.error("Upbit candle API 호출 중 오류 발생", e);
			return Collections.emptyList();
		}
	}
}
