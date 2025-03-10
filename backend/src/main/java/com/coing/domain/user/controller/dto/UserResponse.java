package com.coing.domain.user.controller.dto;

import java.util.UUID;

public record UserResponse(
	UUID id,
	String name,
	String email,
	boolean verified
) {
}
