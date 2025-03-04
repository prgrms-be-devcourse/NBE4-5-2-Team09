package com.coing.standard.utils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.coing.infra.upbit.enums.EnumUpbitRequestType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpbitUtilsTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("makeRequest() 성공 - ORDERBOOK")
    public void successMakeRequestOrderbook() throws Exception {
        // given
        EnumUpbitRequestType requestType = EnumUpbitRequestType.ORDERBOOK;

        // when
        String actualJson = UpbitUtils.makeRequest(requestType);

        // then
		String json = """
			[
			  { "ticket": "orderbook" },
			  {
			    "type": "orderbook",
			    "codes": ["KRW-ADA"],
			    "is_only_snapshot": false,
			    "is_only_realtime": false
			  },
			  { "format": "SIMPLE" }
			]
			""";
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(json);
		String expectedJson = mapper.writeValueAsString(node);
        assertThat(actualJson).isEqualTo(expectedJson);
    }
}
