package com.coing.domain.bookmark.controller.dto;

import java.time.LocalDateTime;

public record BookmarkResponse(
	Long id,
	String code,
	LocalDateTime createAt,
	LocalDateTime updateAt
) {
}
