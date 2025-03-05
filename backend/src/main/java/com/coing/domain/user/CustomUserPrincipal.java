package com.coing.domain.user;

import java.util.UUID;

public record CustomUserPrincipal(UUID id, String email) {
	@Override
	public String toString() {
		return email;
	}
}
