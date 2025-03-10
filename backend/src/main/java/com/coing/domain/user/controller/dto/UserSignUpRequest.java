package com.coing.domain.user.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserSignUpRequest(
	@NotBlank(message = "{name.required}")
	@Size(min = 2, max = 20, message = "{invalid.name.length}")
	String name,

	@NotBlank(message = "{email.required}")
	@Email(message = "{invalid.email.format}") String email,

	@NotBlank(message = "{password.required}")
	@Size(min = 8, max = 20, message = "{invalid.password.length}")
	@Pattern(
		regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$",
		message = "{invalid.password.format}"
	) String password,

	@NotBlank(message = "{invalid.password.confirm}") String passwordConfirm
) {
}