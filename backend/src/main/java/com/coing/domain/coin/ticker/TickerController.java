package com.coing.domain.coin.ticker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.coin.ticker.dto.TickerResponse;
import com.coing.domain.coin.ticker.service.TickerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ticker")
@Tag(name = "Ticker API", description = "현재가 조회 관련 API 엔드포인트")
public class TickerController {

	private final TickerService tickerService;

	@Operation(summary = "특정 마켓 현재가 조회")
	@GetMapping("/{market}")
	public ResponseEntity<TickerResponse> getTicker(@PathVariable("market") String market) {
		TickerResponse response = TickerResponse.from(tickerService.getTicker(market));
		return ResponseEntity.ok(response);
	}
}
