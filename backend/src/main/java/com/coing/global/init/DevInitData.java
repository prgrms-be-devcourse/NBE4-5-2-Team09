package com.coing.global.init;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DevInitData {

	@Bean
	public ApplicationRunner devApplicationRunner() {
		return args -> {
			genApiJsonFile("http://localhost:8080/v3/api-docs/api", "../api_schema.json");
		};
	}

	public void genApiJsonFile(String url, String filename) {
		Path filePath = Path.of(filename);
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.GET()
			.build();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				Files.writeString(filePath, response.body(), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			} else {
				log.error("Error writing Api file {}", response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			log.error("Request failed {}", e.getMessage());
		}
	}
}