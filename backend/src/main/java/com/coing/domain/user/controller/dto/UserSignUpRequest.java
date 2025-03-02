package com.coing.domain.user.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSignUpRequest(
	@NotBlank(message = "name.required") String name,

	@NotBlank(message = "email.required")
	@Email(message = "invalid.email.format") String email,

	@NotBlank(message = "password.required") String password,

	@NotBlank(message = "invalid.password.confirm") String passwordConfirm
) {
}