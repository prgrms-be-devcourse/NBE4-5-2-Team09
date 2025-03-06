package com.coing.domain.bookmark.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.bookmark.controller.dto.BookmarkRequest;
import com.coing.domain.bookmark.controller.dto.BookmarkResponse;
import com.coing.domain.bookmark.service.BookmarkService;
import com.coing.domain.user.CustomUserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@Operation(summary = "북마크 등록")
	@PostMapping("/bookmark")
	public ResponseEntity<BookmarkResponse> addBookmark(
		@RequestBody @Validated BookmarkRequest request,
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		BookmarkResponse response = bookmarkService.addBookmark(request, principal);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "유저 북마크 전체 조회")
	@GetMapping("/bookmarks")
	public ResponseEntity<List<BookmarkResponse>> getBookmarksForCurrentUser(
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		List<BookmarkResponse> responses = bookmarkService.getBookmarksByUser(principal.id());
		return ResponseEntity.ok(responses);
	}

	@Operation(summary = "특정 북마크 삭제")
	@DeleteMapping("/{bookmarkId}")
	public ResponseEntity<Void> deleteBookmark(
		@PathVariable("bookmarkId") Long bookmarkId,
		@AuthenticationPrincipal CustomUserPrincipal principal) {
		bookmarkService.deleteBookmark(principal.id(), bookmarkId);
		return ResponseEntity.noContent().build();
	}
}
