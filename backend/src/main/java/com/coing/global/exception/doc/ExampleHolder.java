package com.coing.global.exception.doc;

import io.swagger.v3.oas.models.examples.Example;
import lombok.Builder;

@Builder
public record ExampleHolder(Example example, int statusCode, String name) {
}
