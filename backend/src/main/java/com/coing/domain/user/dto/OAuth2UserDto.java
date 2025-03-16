package com.coing.domain.user.dto;

import com.coing.domain.user.entity.Provider;

public record OAuth2UserDto(
	Provider provider,
	String name,
	String email
) {
	public static OAuth2UserDto of(Provider provider, String name, String email) {
		return new OAuth2UserDto(provider, name, email);
	}
}
