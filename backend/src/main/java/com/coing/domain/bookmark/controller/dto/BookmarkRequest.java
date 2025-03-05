package com.coing.domain.bookmark.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record BookmarkRequest(
	@NotBlank(message = "{coinCode.required}") String coinCode
) {
}
