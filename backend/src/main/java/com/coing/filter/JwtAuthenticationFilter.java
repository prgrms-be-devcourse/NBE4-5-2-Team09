package com.coing.filter;

import static com.coing.util.Ut.Jwt.*;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
		Long id = ((Number)claims.get("id")).longValue();
		String email = (String)claims.get("email");
		// String authority = (String) claims.get("authority");

		// // 권한 설정
		// Collection<GrantedAuthority> authorities = new ArrayList<>();
		// if (authority != null) {
		// 	authorities.add(new SimpleGrantedAuthority(authority));
		// 	log.debug("User authority set: {}", authority);
		// }

		Authentication authentication = new UsernamePasswordAuthenticationToken(
			email, null);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}
}
