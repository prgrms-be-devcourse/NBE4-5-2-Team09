package com.coing.domain.coin.candle.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coing.domain.coin.candle.entity.CandleSnapshot;

@Repository
public interface CandleSnapshotRepository extends JpaRepository<CandleSnapshot, Long> {
	Page<CandleSnapshot> findAllByCode(String code, Pageable pageable);
}