package com.coing.util;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
	private static final DateTimeFormatter FORMATTER_1 = DateTimeFormatter.ofPattern("HHmmss");
	private static final DateTimeFormatter FORMATTER_2 = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Override
	public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String timeText = p.getText();
		try {
			return LocalTime.parse(timeText, FORMATTER_1);
		} catch (DateTimeParseException e) {
			return LocalTime.parse(timeText, FORMATTER_2);
		}
	}
}
