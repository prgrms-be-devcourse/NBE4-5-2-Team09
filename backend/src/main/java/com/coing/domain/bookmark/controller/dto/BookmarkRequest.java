package com.coing.domain.bookmark.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookmarkRequest(
	@NotNull(message = "{userId.required}") Long userId,
	@NotBlank(message = "{coinCode.required}") String coinCode
) {
}
