package com.coing.domain.user;

import java.util.UUID;

public record CustomUserPrincipal(UUID id) {
	@Override
	public String toString() {
		return id.toString();
	}
}
