package com.coing.domain.user.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.coing.domain.user.dto.CustomOAuth2User;
import com.coing.domain.user.dto.OAuth2UserDto;
import com.coing.domain.user.entity.Provider;
import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		// 사용자 정보 (registrationId + attribute + etc)
		OAuth2User oAuth2User = super.loadUser(userRequest);
		OAuth2UserDto oAuth2UserDto = convertKakaoUserAttribute(oAuth2User);

		// oauth2 인증 방식으로 로그인 혹은 회원가입
		User user = oauth2Login(oAuth2UserDto);

		return new CustomOAuth2User(user);
	}

	private OAuth2UserDto convertKakaoUserAttribute(OAuth2User oAuth2User) {
		Map<String, Object> attribute = oAuth2User.getAttributes();
		Map<String, Object> properties = (Map<String, Object>)attribute.get("properties");
		Map<String, Object> kakaoAccount = (Map<String, Object>)attribute.get("kakao_account");

		return OAuth2UserDto.of(Provider.KAKAO, properties.get("nickname").toString(), kakaoAccount.get("email").toString());
	}

	private User oauth2Login(OAuth2UserDto dto) {
		return userRepository.findByEmail(dto.email()).orElseGet(() -> {
				User savedUser = User.builder()
					.name(dto.name())
					.email(dto.email())
					.password(UUID.randomUUID().toString())
					.verified(true)
					.provider(dto.provider())
					.build();
				return userRepository.save(savedUser);
			});
	}
}
