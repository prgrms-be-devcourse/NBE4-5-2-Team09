package com.coing.domain.news.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.repository.MarketRepository;

@Service
public class NewsService {

	@Value("${naver.client.id}")
	private String clientId;

	@Value("${naver.client.secret}")
	private String clientSecret;

	private final MarketRepository marketRepository;

	public NewsService(MarketRepository marketRepository) {
		this.marketRepository = marketRepository;
	}

	/**
	 * 마켓 코드를 받아서 해당 마켓의 한국어 이름을 쿼리로 사용해 뉴스 검색 API 호출
	 */
	public String searchNewsByMarketCode(String marketCode, int display, int start, String sort, String format) {
		// MarketRepository를 사용해 마켓 정보 조회
		Market market = marketRepository.findByCode(marketCode)
			.orElseThrow(() -> new NoSuchElementException("Market not found for code: " + marketCode));
		String query = market.getKoreanName(); // 한국어 이름을 검색어로 사용
		return searchNews(query, display, start, sort, format);
	}

	/**
	 * 네이버 뉴스 검색 API 호출 (JSON)
	 *
	 * @param query   검색어 ()
	 * @param display 한 번에 표시할 결과 개수
	 * @param start   검색 시작 위치
	 * @param sort    정렬 방식 ("sim" 또는 "date")
	 * @param format  결과 포맷 ("json" 또는 "xml")
	 * @return API 응답 문자열
	 */
	public String searchNews(String query, int display, int start, String sort, String format) {
		String encodedQuery;
		try {
			encodedQuery = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("검색어 인코딩 실패", e);
		}

		String apiURL = "https://openapi.naver.com/v1/search/news." + format +
			"?query=" + encodedQuery +
			"&display=" + display +
			"&start=" + start +
			"&sort=" + sort;

		Map<String, String> requestHeaders = new HashMap<>();
		requestHeaders.put("X-Naver-Client-Id", clientId);
		requestHeaders.put("X-Naver-Client-Secret", clientSecret);

		return get(apiURL, requestHeaders);
	}

	private String get(String apiUrl, Map<String, String> requestHeaders) {
		HttpURLConnection con = connect(apiUrl);
		try {
			con.setRequestMethod("GET");
			for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
				con.setRequestProperty(header.getKey(), header.getValue());
			}

			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
				return readBody(con.getInputStream());
			} else { // 오류 발생
				return readBody(con.getErrorStream());
			}
		} catch (IOException e) {
			throw new RuntimeException("API 요청과 응답 실패", e);
		} finally {
			con.disconnect();
		}
	}

	private HttpURLConnection connect(String apiUrl) {
		try {
			URL url = new URL(apiUrl);
			return (HttpURLConnection)url.openConnection();
		} catch (MalformedURLException e) {
			throw new RuntimeException("API URL이 잘못되었습니다: " + apiUrl, e);
		} catch (IOException e) {
			throw new RuntimeException("연결 실패: " + apiUrl, e);
		}
	}

	private String readBody(InputStream body) {
		InputStreamReader streamReader = new InputStreamReader(body);
		try (BufferedReader lineReader = new BufferedReader(streamReader)) {
			StringBuilder responseBody = new StringBuilder();
			String line;
			while ((line = lineReader.readLine()) != null) {
				responseBody.append(line);
			}
			return responseBody.toString();
		} catch (IOException e) {
			throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
		}
	}
}
