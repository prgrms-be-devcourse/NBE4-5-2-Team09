package com.coing.domain.user.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
	@NotBlank(message = "{email.required}")

	@Email(message = "{invalid.email.format}") String email,

	@NotBlank(message = "{password.required}") String password
) {
}
