package com.coing.domain.bookmark.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.bookmark.controller.dto.BookmarkRequest;
import com.coing.domain.bookmark.controller.dto.BookmarkResponse;
import com.coing.domain.bookmark.controller.dto.BookmarkUpdateRequest;
import com.coing.domain.bookmark.entity.Bookmark;
import com.coing.domain.bookmark.repository.BookmarkRepository;
import com.coing.domain.user.entity.User;
import com.coing.domain.user.repository.UserRepository;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final UserRepository userRepository;
	private final MessageUtil messageUtil;

	@Transactional
	public BookmarkResponse addBookmark(BookmarkRequest request) {
		Long userId = request.userId();
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
			user.getId(),
			savedBookmark.getCoinCode(),
			savedBookmark.getCreateAt(),
			savedBookmark.getUpdateAt()
		);
	}

	@Transactional(readOnly = true)
	public List<BookmarkResponse> getBookmarksByUser(Long userId) {
		List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);
		return bookmarks.stream()
			.map(b -> new BookmarkResponse(
				b.getId(),
				b.getUser().getId(),
				b.getCoinCode(),
				b.getCreateAt(),
				b.getUpdateAt()
			))
			.collect(Collectors.toList());
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
			updated.getUser().getId(),
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
