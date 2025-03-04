package com.coing.domain.coin.orderbook.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coing.domain.coin.orderbook.entity.OrderbookSnapshot;

@Repository
public interface OrderbookSnapshotRepository extends JpaRepository<OrderbookSnapshot, Long> {
    OrderbookSnapshot findTopByCodeOrderByTimestampDesc(String code);
}