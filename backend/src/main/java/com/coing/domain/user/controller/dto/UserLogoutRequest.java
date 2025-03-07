package com.coing.domain.user.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLogoutRequest(
	@NotBlank(message = "{email.required}")
	@Email(message = "{invalid.email.format}") String email
) {
}
