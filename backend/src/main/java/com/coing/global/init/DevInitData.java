package com.coing.global.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

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
			genApiJsonFile("http://localhost:8080/v3/api-docs/api", "schema.json");

			List<String> command = new ArrayList<>();
			if (isWindows()) {
				command.add("cmd.exe");
				command.add("/c");
				command.add(
					"mkdir -p ../frontend/src/lib/api && move schema.json ../frontend/src/lib/api && cd ../frontend && npm run generate-api-schema");
			} else {
				command.add("sh");
				command.add("-c");
				command.add(
					"mkdir -p ../frontend/src/lib/api && mv schema.json ../frontend/src/lib/api && cd ../frontend && npm run generate-api-schema");
			}

			runCmd(command);
		};
	}

	/**
	 * 현재 OS가 Windows인지 확인하는 메서드
	 */
	private boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	public void runCmd(List<String> command) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);

			Process process = processBuilder.start();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			}
		} catch (Exception e) {
			log.error("Error while running command {}", e.getMessage());
		}
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
