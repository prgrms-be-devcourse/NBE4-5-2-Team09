package com.coing.domain.user.controller.dto;

public record UserResponse(
	Long id,
	String name,
	String email
) {
}
