package com.coing.domain.bookmark.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.coing.domain.bookmark.controller.dto.BookmarkRequest;
import com.coing.domain.bookmark.controller.dto.BookmarkResponse;
import com.coing.domain.bookmark.controller.dto.BookmarkUpdateRequest;
import com.coing.domain.bookmark.entity.Bookmark;
import com.coing.domain.bookmark.repository.BookmarkRepository;
import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;
import com.coing.domain.user.service.AuthTokenService;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final UserRepository userRepository;
	private final MessageUtil messageUtil;
	private final AuthTokenService authTokenService;

	/**
	 * 현재 HttpServletRequest의 Authorization 헤더에서 JWT 토큰을 추출하고,
	 * AuthTokenService를 사용하여 클레임에서 사용자 id (UUID)를 반환합니다.
	 */
	private UUID getCurrentUserIdFromRequest() {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new BusinessException(messageUtil.resolveMessage("empty.token.provided"), HttpStatus.UNAUTHORIZED,
				"");
		}
		String token = authHeader.substring("Bearer ".length());
		var claims = authTokenService.verifyToken(token);
		if (claims == null || claims.get("id") == null) {
			throw new BusinessException(messageUtil.resolveMessage("invalid.token"), HttpStatus.UNAUTHORIZED, "");
		}
		String userIdStr = claims.get("id").toString();
		return UUID.fromString(userIdStr);
	}

	@Transactional
	public BookmarkResponse addBookmark(BookmarkRequest request) {
		UUID userId = getCurrentUserIdFromRequest();
		String coinCode = request.coinCode();

		if (bookmarkRepository.existsByUserIdAndCoinCode(userId, coinCode)) {
			throw new BusinessException(
				messageUtil.resolveMessage("bookmark.already.exists"),
				HttpStatus.BAD_REQUEST
			);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(
				messageUtil.resolveMessage("member.not.found"),
				HttpStatus.NOT_FOUND
			));

		Bookmark bookmark = Bookmark.builder()
			.user(user)
			.coinCode(coinCode)
			.createAt(LocalDateTime.now())
			.build();

		Bookmark savedBookmark = bookmarkRepository.save(bookmark);
		return new BookmarkResponse(
			savedBookmark.getId(),
			savedBookmark.getCoinCode(),
			savedBookmark.getCreateAt(),
			savedBookmark.getUpdateAt()
		);
	}

	@Transactional(readOnly = true)
	public List<BookmarkResponse> getBookmarksByUser(UUID userId) {
		List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
		return bookmarks.stream()
			.map(b -> new BookmarkResponse(
				b.getId(),
				b.getCoinCode(),
				b.getCreateAt(),
				b.getUpdateAt()
			))
			.collect(Collectors.toList());
	}

	// 서비스 내에서는 인증된 사용자의 북마크 조회도 쉽게 처리할 수 있도록 별도 메서드 추가
	@Transactional(readOnly = true)
	public List<BookmarkResponse> getBookmarksForCurrentUser() {
		UUID userId = getCurrentUserIdFromRequest();
		return getBookmarksByUser(userId);
	}

	@Transactional
	public BookmarkResponse updateBookmark(BookmarkUpdateRequest request) {
		Bookmark bookmark = bookmarkRepository.findById(request.bookmarkId())
			.orElseThrow(() -> new BusinessException(
				messageUtil.resolveMessage("bookmark.not.found"),
				HttpStatus.NOT_FOUND
			));

		bookmark.updateCoinCode(request.coinCode());
		Bookmark updated = bookmarkRepository.save(bookmark);
		return new BookmarkResponse(
			updated.getId(),
			updated.getCoinCode(),
			updated.getCreateAt(),
			updated.getUpdateAt()
		);
	}

	@Transactional
	public void deleteBookmark(Long bookmarkId) {
		if (!bookmarkRepository.existsById(bookmarkId)) {
			throw new BusinessException(
				messageUtil.resolveMessage("bookmark.not.found"),
				HttpStatus.NOT_FOUND
			);
		}
		bookmarkRepository.deleteById(bookmarkId);
	}
}
