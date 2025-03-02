package com.coing.infra.upbit.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Upbit WebSocket Request Type Field
 * 수신하고 싶은 시세 정보를 나열하는 필드
 * `is_only_snapshot`, `is_only_realtime` 필드는 생략 가능하며 모두 생략시 스냅샷과 실시간 데이터 모두를 수신한다.
 * 하나의 요청에 `{type field}`는 여러개를 명시할 수 있다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitWebSocketTypeDto {
	@NotBlank
    private String type;
	@NotEmpty
    private List<String> codes;
	@JsonProperty("is_only_snapshot")
    private Boolean isOnlySnapshot;
	@JsonProperty("is_only_realtime")
    private Boolean isOnlyRealtime;
}
