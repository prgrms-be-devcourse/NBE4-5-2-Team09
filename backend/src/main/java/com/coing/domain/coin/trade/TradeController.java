package com.coing.domain.coin.trade;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.coin.trade.dto.TradeResponse;
import com.coing.domain.coin.trade.service.TradeService;
import com.coing.global.exception.doc.ApiErrorCodeExamples;
import com.coing.global.exception.doc.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trade")
@Tag(name = "Trade API", description = "체결 조회 관련 API 엔드포인트")
public class TradeController {

	private final TradeService tradeService;

	@Operation(summary = "특정 마켓 체결 내역 조회")
	@GetMapping("/{market}")
	@ApiErrorCodeExamples({ErrorCode.TRADE_NOT_FOUND})
	public ResponseEntity<TradeResponse> getTrades(@PathVariable("market") String market) {
		TradeResponse response = TradeResponse.from(tradeService.getTrades(market));
		return ResponseEntity.ok(response);
	}
}
