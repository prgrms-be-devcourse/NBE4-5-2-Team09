package com.coing.domain.user.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record SignOutRequest(@NotBlank(message = "비밀번호를 입력해 주세요.") String password) {
}
