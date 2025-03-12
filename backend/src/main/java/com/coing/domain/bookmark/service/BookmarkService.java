package com.coing.domain.bookmark.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.coing.util.PageUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final MarketRepository marketRepository;
	private final UserRepository userRepository;
	private final MessageUtil messageUtil;

	@Transactional
	public void addBookmark(BookmarkRequest request, CustomUserPrincipal principal) {
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
			.build();

		bookmarkRepository.save(bookmark);
		//return BookmarkResponse.of(bookmark);
	}

	@Transactional(readOnly = true)
	public Page<BookmarkResponse> getBookmarksByQuote(UUID userId, String quote, Pageable pageable) {
		List<Bookmark> bookmarks = bookmarkRepository.findByUserIdAndQuote(userId, quote);

		List<BookmarkResponse> responses = bookmarks.stream()
			.map(BookmarkResponse::of)
			.toList();

		return PageUtil.paginate(responses, pageable);
	}

	@Transactional
	public void deleteBookmark(UUID userId, String marketCode) {
		Bookmark bookmark = bookmarkRepository.findByMarketCode(marketCode);
		if (bookmark == null) {
			throw new BusinessException(messageUtil.resolveMessage("bookmark.not.found"),
				HttpStatus.NOT_FOUND);
		}

		// 인증된 사용자가 북마크의 소유자인지 확인
		if (!bookmark.getUser().getId().equals(userId)) {
			throw new BusinessException(messageUtil.resolveMessage("bookmark.access.denied"),
				HttpStatus.FORBIDDEN);
		}

		bookmarkRepository.delete(bookmark);
	}
}
