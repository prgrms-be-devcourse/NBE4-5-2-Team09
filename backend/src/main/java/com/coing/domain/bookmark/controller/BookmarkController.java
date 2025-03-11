package com.coing.domain.bookmark.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.bookmark.controller.dto.BookmarkRequest;
import com.coing.domain.bookmark.controller.dto.BookmarkResponse;
import com.coing.domain.bookmark.service.BookmarkService;
import com.coing.domain.coin.common.dto.PagedResponse;
import com.coing.domain.user.CustomUserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@Operation(summary = "북마크 등록", security = @SecurityRequirement(name = "bearerAuth"))
	@PostMapping("/bookmark")
	public ResponseEntity<BookmarkResponse> addBookmark(
		@RequestBody @Validated BookmarkRequest request,
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		BookmarkResponse response = bookmarkService.addBookmark(request, principal);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "유저 북마크 조회", security = @SecurityRequirement(name = "bearerAuth"))
	@GetMapping("/bookmarks/{quote}")
	public ResponseEntity<PagedResponse<BookmarkResponse>> getBookmarksByQuote(
		@AuthenticationPrincipal CustomUserPrincipal principal,
		@PathVariable String quote,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "9") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<BookmarkResponse> result = bookmarkService.getBookmarksByQuote(principal.id(), quote, pageable);

		PagedResponse<BookmarkResponse> response = new PagedResponse<>(
			result.getNumber(),
			result.getSize(),
			result.getTotalElements(),
			result.getTotalPages(),
			result.getContent()
		);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "특정 북마크 삭제", security = @SecurityRequirement(name = "bearerAuth"))
	@DeleteMapping("/bookmark/{bookmarkId}")
	public ResponseEntity<Void> deleteBookmark(
		@PathVariable("bookmarkId") Long bookmarkId,
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		bookmarkService.deleteBookmark(principal.id(), bookmarkId);
		return ResponseEntity.noContent().build();
	}
}
