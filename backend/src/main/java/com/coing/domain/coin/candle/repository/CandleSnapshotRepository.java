package com.coing.domain.coin.candle.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coing.domain.coin.candle.entity.CandleSnapshot;

@Repository
public interface CandleSnapshotRepository extends JpaRepository<CandleSnapshot, Long> {
	Optional<CandleSnapshot> findTopByCodeOrderBySnapshotTimestampDesc(String code);
}