package com.coing.domain.user.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.coing.domain.user.entity.User;

import lombok.Getter;

@Getter
public class CustomOAuth2User implements OAuth2User {

	private final User user;

	public CustomOAuth2User(User user) {
		this.user = user;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(user.getAuthority().name()));
		return authorities;
	}

	@Override
	public String getName() {
		return user.getId().toString();
	}
}
