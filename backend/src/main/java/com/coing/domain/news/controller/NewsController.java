package com.coing.domain.news.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.news.service.NewsService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api")
public class NewsController {

	private final NewsService newsService;

	public NewsController(NewsService newsService) {
		this.newsService = newsService;
	}

	/**
	 * 마켓 코드로 뉴스 조회 (검색어는 해당 마켓의 한국 이름 사용)
	 */
	@Operation(summary = "마켓 코드로 뉴스 조회 (검색어는 해당 마켓의 한국 이름 사용)")
	@GetMapping("/news")
	public ResponseEntity<String> searchNews(
		@RequestParam(name = "market") String marketCode,
		@RequestParam(name = "display", defaultValue = "100") int display,
		@RequestParam(name = "start", defaultValue = "1") int start,
		@RequestParam(name = "sort", defaultValue = "sim") String sort,
		@RequestParam(name = "format", defaultValue = "json") String format) {

		String result = newsService.searchNewsByMarketCode(marketCode, display, start, sort, format);
		return ResponseEntity.ok(result);
	}
}
