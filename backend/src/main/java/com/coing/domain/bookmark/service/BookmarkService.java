package com.coing.domain.bookmark.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.bookmark.controller.dto.BookmarkRequest;
import com.coing.domain.bookmark.controller.dto.BookmarkResponse;
import com.coing.domain.bookmark.entity.Bookmark;
import com.coing.domain.bookmark.repository.BookmarkRepository;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.repository.MarketRepository;
import com.coing.domain.user.CustomUserPrincipal;
import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final MarketRepository marketRepository;
	private final UserRepository userRepository;
	private final MessageUtil messageUtil;

	@Transactional
	public BookmarkResponse addBookmark(BookmarkRequest request, CustomUserPrincipal principal) {
		UUID userId = principal.id();
		String coinCode = request.coinCode();

		// 기존 북마크 존재 여부 확인
		if (bookmarkRepository.existsByUserIdAndMarketCode(userId, coinCode)) {
			throw new BusinessException(messageUtil.resolveMessage("bookmark.already.exists"),
				HttpStatus.BAD_REQUEST);
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(messageUtil.resolveMessage("member.not.found"),
				HttpStatus.NOT_FOUND));

		Market market = marketRepository.findById(coinCode)
			.orElseThrow(() -> new BusinessException(messageUtil.resolveMessage("market.not.found"),
				HttpStatus.NOT_FOUND));

		Bookmark bookmark = Bookmark.builder()
			.user(user)
			.market(market)
			.createAt(LocalDateTime.now())
			.build();

		Bookmark savedBookmark = bookmarkRepository.save(bookmark);
		return new BookmarkResponse(
			savedBookmark.getId(),
			savedBookmark.getMarket().getCode(),
			savedBookmark.getCreateAt()
		);
	}

	@Transactional(readOnly = true)
	public List<BookmarkResponse> getBookmarksByUser(UUID userId) {
		List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
		return bookmarks.stream()
			.map(b -> new BookmarkResponse(
				b.getId(),
				b.getMarket().getCode(),
				b.getCreateAt()
			))
			.collect(Collectors.toList());
	}

	@Transactional
	public void deleteBookmark(UUID userId, Long bookmarkId) {
		Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
			.orElseThrow(() -> new BusinessException(messageUtil.resolveMessage("bookmark.not.found"),
				HttpStatus.NOT_FOUND));

		// 인증된 사용자가 북마크의 소유자인지 확인
		if (!bookmark.getUser().getId().equals(userId)) {
			throw new BusinessException(messageUtil.resolveMessage("bookmark.access.denied"),
				HttpStatus.FORBIDDEN);
		}

		bookmarkRepository.deleteById(bookmarkId);
	}
}
