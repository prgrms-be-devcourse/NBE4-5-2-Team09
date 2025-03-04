package com.coing.domain.bookmark.controller.dto;

import java.time.LocalDateTime;

public record BookmarkResponse(
	Long id,
	Long userId,
	String coinCode,
	LocalDateTime createAt,
	LocalDateTime updateAt
) {
}
