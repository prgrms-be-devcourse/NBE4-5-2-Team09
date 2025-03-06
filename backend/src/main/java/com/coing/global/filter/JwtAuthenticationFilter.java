package com.coing.global.filter;

import static com.coing.util.Ut.Jwt.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.coing.domain.user.CustomUserPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Value("${custom.jwt.secret-key}")
	private String secretKey;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authorizationHeader.substring(7);

		if (!isValidToken(secretKey, token)) {
			filterChain.doFilter(request, response);
			return;
		}

		Map<String, Object> claims = getPayload(secretKey, token);
		UUID id = UUID.fromString(claims.get("id").toString());
		String email = (String)claims.get("email");
		String name = (String)claims.get("name");

		// 커스텀 Principal 객체 생성 (이제 이름도 포함)
		CustomUserPrincipal principal = new CustomUserPrincipal(id, email, name);
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}
}
