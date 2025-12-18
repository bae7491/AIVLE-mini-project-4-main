package com.example.back.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class BookCoverStorageService {

    private final String coverPath = "./back/uploads/bookcovers";

    // DB에 넣을 때 앞에 붙일 서버 주소
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * AI 이미지 URL을 받아서 로컬에 {bookId}.png 로 저장하고,
     * API에서 사용할 상대 경로(/api/books/cover/{bookId})를 반환.
     */
    public String saveCoverFromUrl(String imageUrl, Long bookId) {
        HttpURLConnection conn = null;

        try {
            // 0) 먼저 HTTP 연결 열기
            URL url = new URL(imageUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setInstanceFollowRedirects(false);

            int status = conn.getResponseCode();
            log.info("이미지 URL 응답 코드: {} -> {}", status, imageUrl);

            // ✅ 200대(정상) 아니면 바로 실패 처리
            if (status < 200 || status >= 300) {
                log.warn("유효하지 않은 이미지 URL 상태코드, 저장 중단: status={}, url={}", status, imageUrl);
                return null;
            }

            // 1) 디렉터리 보장
            Path dir = Paths.get(coverPath);
            Files.createDirectories(dir);

            // 2) 파일 경로 (AI가 png라서 .png로 저장)
            Path filePath = dir.resolve(bookId + ".png");

            log.info("도서 표지 이미지 다운로드 시작: url={}, path={}", imageUrl, filePath);

            // 3) 응답 스트림에서 읽어서 파일로 복사
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("도서 표지 이미지 저장 완료: path={}", filePath);

            // 4) DB에 저장할 전체 URL 생성 (기존 형식 그대로)
            String publicUrl = baseUrl + "/api/books/cover/" + bookId;
            log.info("도서 표지 public URL 생성: {}", publicUrl);

            return publicUrl;

        } catch (Exception e) {
            log.error("도서 표지 이미지 저장 실패: url={}, bookId={}, error={}", imageUrl, bookId, e.toString());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}