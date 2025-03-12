package com.coing.domain.bookmark.controller.dto;

import com.coing.domain.bookmark.entity.Bookmark;

public record BookmarkResponse(
	String code,
	String koreanName,
	String englishName,
	Boolean isBookmarked
) {
	public static BookmarkResponse of(Bookmark bookmark) {
		return new BookmarkResponse(
			bookmark.getMarket().getCode(),
			bookmark.getMarket().getKoreanName(),
			bookmark.getMarket().getEnglishName(),
			true
		);
	}
}
