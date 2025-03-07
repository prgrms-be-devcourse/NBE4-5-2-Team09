package com.coing.domain.user.controller.dto;

import java.util.UUID;

public record UserSignupResponse(String message, String name, String email, UUID userId) {
}
