package com.coing.domain.coin.common.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
	@NotNull
	private int page;

	@NotNull
	private int size;

	@NotNull
	private long totalElements;

	@NotNull
	private int totalPages;

	@NotEmpty
	private List<T> content;
}