package com.coing.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("t1: 일반 회원 가입 - 정상 동작 테스트")
	void t1() {
		String name = "테스트";
		String email = "test@test.com";
		String password = "test";
		String passwordConfirm = "test";

		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

		User savedUser = User.builder()
			.name(name)
			.email(email)
			.password("encodedPassword")
			.build();
		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		User result = userService.join(name, email, password, passwordConfirm);

		assertNotNull(result);
		assertEquals(email, result.getEmail());
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("t2: 일반 회원 가입 - 비밀번호 불일치 테스트")
	void t2() {
		// given
		String name = "테스트";
		String email = "test@test.com";
		String password = "test";
		String passwordConfirm = "test2";

		// when & then
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userService.join(name, email, password, passwordConfirm);
		});
		assertEquals("password.mismatch", exception.getMessage());
	}

	@Test
	@DisplayName("t3: 일반 회원 가입 - 중복 이메일 테스트")
	void t3() {
		// given
		String name = "테스트";
		String email = "test@test.com";
		String password = "test";
		String passwordConfirm = "test";

		User existingUser = new User();
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

		// when & then
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			userService.join(name, email, password, passwordConfirm);
		});
		assertEquals("already.registered.email", exception.getMessage());
	}
}
