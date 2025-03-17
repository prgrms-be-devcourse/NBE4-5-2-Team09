package com.coing.domain.coin.ticker.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record TickersRequest(@NotNull List<String> markets) {
}
