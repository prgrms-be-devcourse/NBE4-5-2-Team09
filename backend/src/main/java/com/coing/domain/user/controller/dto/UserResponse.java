package com.coing.domain.user.controller.dto;

import java.util.UUID;

import com.coing.domain.user.entity.User;

public record UserResponse(
	UUID id,
	String name,
	String email,
	boolean verified
) {
	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.isVerified());
	}
}
