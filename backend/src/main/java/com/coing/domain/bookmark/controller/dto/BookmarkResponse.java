package com.coing.domain.bookmark.controller.dto;

import java.time.LocalDateTime;

import com.coing.domain.bookmark.entity.Bookmark;

public record BookmarkResponse(
	Long id,
	String code,
	String koreanName,
	String englishName,
	LocalDateTime createAt
) {
	public static BookmarkResponse of(Bookmark bookmark) {
		return new BookmarkResponse(
			bookmark.getId(),
			bookmark.getMarket().getCode(),
			bookmark.getMarket().getKoreanName(),
			bookmark.getMarket().getEnglishName(),
			bookmark.getCreatedAt()
		);
	}
}
