package com.coing.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coing.domain.chat.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
	int countByMessageId(Long messageId);
}
