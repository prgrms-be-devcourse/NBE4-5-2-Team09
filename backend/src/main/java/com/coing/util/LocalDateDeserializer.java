package com.coing.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
	private static final DateTimeFormatter FORMATTER_1 = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String dateText = p.getText();
		try {
			return LocalDate.parse(dateText, FORMATTER_1);
		} catch (DateTimeParseException e) {
			return LocalDate.parse(dateText, FORMATTER_2);
		}
	}
}
