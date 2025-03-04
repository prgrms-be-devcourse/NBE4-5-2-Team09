package com.coing.domain.bookmark.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookmarkUpdateRequest(
	@NotNull(message = "{bookmarkId.required}") Long bookmarkId,
	@NotBlank(message = "{coinCode.required}") String coinCode
) {
}
