package com.coing.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class UtilTest {
	@Test
	@DisplayName("PageUtil - 빈 리스트 페이징")
	void paginateEmptyList() {
		// given
		List<String> list = List.of();
		Pageable pageable = PageRequest.of(0, 5);

		// when
		Page<String> page = PageUtil.paginate(list, pageable);

		// then
		assertThat(page.getContent()).isEmpty();
		assertThat(page.getTotalElements()).isEqualTo(0);
	}

	@Test
	@DisplayName("PageUtil - 전체 리스트가 한 페이지에 들어가는 경우")
	void paginateFullList() {
		// given
		List<Integer> list = Arrays.asList(1, 2, 3, 4);
		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<Integer> page = PageUtil.paginate(list, pageable);

		// then
		assertThat(page.getContent()).containsExactly(1, 2, 3, 4);
		assertThat(page.getTotalElements()).isEqualTo(4);
	}

	@Test
	@DisplayName("PageUtil - 리스트의 일부만 포함된 페이지 반환")
	void paginatePartialPage() {
		// given
		List<String> list = Arrays.asList("A", "B", "C", "D", "E", "F");
		Pageable pageable = PageRequest.of(1, 2);

		// when
		Page<String> page = PageUtil.paginate(list, pageable);

		// then
		assertThat(page.getContent()).containsExactly("C", "D");
		assertThat(page.getTotalElements()).isEqualTo(6);
	}
}
