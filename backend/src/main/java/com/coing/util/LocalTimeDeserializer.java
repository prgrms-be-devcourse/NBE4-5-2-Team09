package com.coing.util;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

	@Override
	public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		return LocalTime.parse(p.getText(), FORMATTER);
	}
}
