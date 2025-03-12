package com.coing.domain.user.controller.dto;

public record PasswordResetConfirmRequest(String newPassword, String newPasswordConfirm) {
}
