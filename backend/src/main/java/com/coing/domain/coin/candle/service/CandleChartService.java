package com.coing.domain.coin.candle.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.candle.dto.CandleChartDto;
import com.coing.domain.coin.candle.entity.CandleSnapshot;
import com.coing.domain.coin.candle.enums.CandleInterval;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CandleChartService {

	private final MessageUtil messageUtil;

	/**
	 * 캔들 스냅샷 리스트를 지정된 CandleInterval 단위로 집계하여 CandleChartDto를 생성합니다.
	 * 예외 상황(입력 데이터 null, 빈 리스트 등)은 예외 처리하고 로그에 남깁니다.
	 */
	public CandleChartDto aggregateCandles(List<CandleSnapshot> snapshots, CandleInterval interval) {
		if (snapshots == null || snapshots.isEmpty()) {
			throw new BusinessException(messageUtil.resolveMessage("snapshot.not.found"), HttpStatus.NOT_FOUND);
		}
		// 입력 데이터 복사 후 정렬 (불변 리스트의 경우에도 가능)
		List<CandleSnapshot> sortedSnapshots = new ArrayList<>(snapshots);
		sortedSnapshots.sort(Comparator.comparing(CandleSnapshot::getSnapshotTimestamp));

		try {
			// 집계 시간: 가장 첫 캔들의 시간을 기준으로 interval 단위로 정규화
			LocalDateTime baseTime = normalizeTimestamp(sortedSnapshots.get(0).getSnapshotTimestamp(), interval);
			String type = "candle." + interval.toString().toLowerCase();
			String code = sortedSnapshots.get(0).getCode();

			double open = sortedSnapshots.get(0).getOpeningPrice();
			double close = sortedSnapshots.get(sortedSnapshots.size() - 1).getTradePrice();

			double high = sortedSnapshots.stream().mapToDouble(CandleSnapshot::getHighPrice).max().orElseThrow();
			double low = sortedSnapshots.stream().mapToDouble(CandleSnapshot::getLowPrice).min().orElseThrow();
			double volume = sortedSnapshots.stream().mapToDouble(CandleSnapshot::getCandleAccTradeVolume).sum();

			return CandleChartDto.builder()
				.type(type)
				.code(code)
				.candleDateTime(baseTime)
				.openingPrice(open)
				.highPrice(high)
				.lowPrice(low)
				.closingPrice(close)
				.volume(volume)
				.build();
		} catch (Exception e) {
			log.error("캔들 집계 중 오류 발생", e);
			throw new RuntimeException("캔들 집계 실패", e);
		}
	}

	/**
	 * 입력된 LocalDateTime을 주어진 interval 단위로 정규화합니다.
	 * 예: MINUTE 단위라면 초, 나노초를 0으로 만들어 정각의 시간 반환
	 */
	private LocalDateTime normalizeTimestamp(LocalDateTime timestamp, CandleInterval interval) {
		switch (interval) {
			case SECOND:
				return timestamp.truncatedTo(ChronoUnit.SECONDS);
			case MINUTE:
				return timestamp.truncatedTo(ChronoUnit.MINUTES);
			case HOUR:
				return timestamp.truncatedTo(ChronoUnit.HOURS);
			case DAY:
				return timestamp.toLocalDate().atStartOfDay();
			case WEEK:
				// ISO 기준으로 해당 주의 시작(월요일)
				LocalDateTime dayStart = timestamp.toLocalDate().atStartOfDay();
				int dayOfWeek = dayStart.getDayOfWeek().getValue(); // 1=월, 7=일
				return dayStart.minusDays(dayOfWeek - 1);
			case MONTH:
				return timestamp.withDayOfMonth(1).toLocalDate().atStartOfDay();
			case YEAR:
				return timestamp.withDayOfYear(1).toLocalDate().atStartOfDay();
			default:
				throw new IllegalArgumentException("지원되지 않는 캔들 간격: " + interval);
		}
	}
}
