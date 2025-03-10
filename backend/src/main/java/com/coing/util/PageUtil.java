package com.coing.util;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageUtil {
	public static <T> Page<T> paginate(List<T> list, Pageable pageable) {
		int start = (int)pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), list.size());
		return new PageImpl<>(list.subList(start, end), pageable, list.size());
	}
}