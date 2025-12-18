package com.example.back.controller;

import java.io.File;

import com.example.back.DTO.*;
import com.example.back.service.BookService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestController
@RequestMapping("/api/books") 
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final String coverPath = "./back/uploads/bookcovers/";

    @GetMapping("/cover/{bookId}")
    @SuppressWarnings("null")
    public ResponseEntity<?> getBookCover(@PathVariable("bookId") Long bookId) {
        /**
         * 책 커버 이미지 반환 API
         * - 전달받은 bookId로 서버 로컬에 저장된 책 표지 이미지를 조회하여 반환합니다.
         * - 경로 규칙: {coverPath}/{bookId}.jpg
         *
         * @param bookId Long
         *   - URL Path Variable: 요청한 도서의 ID
         *
         * @return ResponseEntity<?>
         *   - 200: 이미지 파일 반환 (Content-Type: image/jpeg)
         *   - 404: 해당 bookId의 이미지 파일이 존재하지 않음
         */
        log.info("커버 이미지 요청: bookId={}", bookId);

        String filePath = coverPath + bookId + ".png";
        File file = new File(filePath);

        // 이미지 파일 존재 여부 확인
        if (!file.exists()) {
            log.warn("커버 이미지 파일 없음: path={}", filePath);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                        java.util.Map.of(
                            "status", "error",
                            "message", "이미지를 찾을 수 없습니다."
                        )
                    );
        }

        // 파일이 존재할 때
        log.info("커버 이미지 파일 반환 준비 완료: path={}", filePath);

        Resource resource = new FileSystemResource(file);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        /**
         * 도서 목록 조회 API (GET)
         *
         * <동작 개요>
         * - 페이지 번호와 페이지 크기를 기준으로 도서 목록을 조회하여 응답으로 반환한다.
         *
         * 요청 정보
         * - @RequestParam int page
         *   : 조회할 페이지 번호 (기본값 1)
         * - @RequestParam int size
         *   : 한 페이지당 조회할 도서 수 (기본값 10)
         *
         * 응답 형식 (ResponseEntity<ApiResponse<BookListResponse>>)
         * - 200: 도서 목록 조회 성공
         * - 400: 잘못된 페이지 번호 등 잘못된 요청
         * - 500: 서버 내부 오류 발생 시
         */
        BookListResponse data = bookService.getBooks(page - 1, size);

        log.info("도서 목록 조회 성공: page={}, totalPages={}", data.getPage(), data.getTotalPages());

        return ResponseEntity.ok(
                new ApiResponse<>("success", "도서목록조회성공", data)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<BookListResponse>> searchBooksByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        /**
         * 도서 제목 검색 API (GET)
         *
         * <동작 개요>
         * - 입력받은 제목 키워드를 기준으로 도서 목록을 검색하고, 페이지 정보를 포함하여 결과를 반환한다.
         *
         * 요청 정보
         * - @RequestParam String title
         *   : 검색할 도서 제목 키워드
         * - @RequestParam int page
         *   : 조회할 페이지 번호 (기본값 1, 프론트 기준)
         * - @RequestParam int size
         *   : 한 페이지당 조회할 도서 수 (기본값 10)
         *
         * 응답 형식 (ResponseEntity<ApiResponse<BookListResponse>>)
         * - 200: 도서 제목 검색 성공
         * - 400: 검색어 미입력, 잘못된 페이지 요청 등 잘못된 요청
         * - 500: 서버 내부 오류 발생 시
         */
        log.info("도서 제목 검색 요청: title={}, page={}, size={}", title, page - 1, size);

        // page는 프론트 기준 1부터, 서비스/DB는 0부터 사용하므로 -1
        BookListResponse data = bookService.searchBooksByTitle(title, page - 1, size);

        log.info("도서 제목 검색 성공: title={}, page={}, totalPages={}",
                title, data.getPage(), data.getTotalPages());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "도서제목검색성공",
                        data
                )
        );
    }

    @GetMapping("/detail/{bookId}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBookDetail(
            @PathVariable Long bookId
    ) {
        /**
         * 도서 상세 조회 API (GET)
         *
         * <동작 개요>
         * - 전달받은 도서 ID를 기준으로 도서 상세 정보를 조회하여 응답으로 반환한다.
         *
         * 요청 정보
         * - @PathVariable Long bookId
         *   : 상세 조회할 도서의 고유 식별자(ID)
         *
         * 응답 형식 (ResponseEntity<ApiResponse<BookDetailResponse>>)
         * - 200: 도서 상세 조회 성공
         * - 404: 해당 ID의 도서가 존재하지 않을 경우 (IllegalArgumentException 발생)
         * - 500: 서버 내부 오류 발생 시
         */
        log.info("도서 상세 조회 요청: bookId={}", bookId);

        BookDetailResponse data = bookService.getBookDetail(bookId);

        log.info("도서 상세 조회 성공: bookId={}", bookId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "도서 상세 조회 성공",
                        data
                )
        );
    }


    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createBook(
            @RequestAttribute("userId") String userId,
            @RequestBody BookCreateRequest req
    ) {
        /**
         * 도서 등록 API (POST)
         *
         * <동작 개요>
         * - JWT 필터에서 검증된 userId(@RequestAttribute)와 요청 본문(BookCreateRequest)을 전달받아
         *   도서 등록 서비스(createBook)를 호출하고, 결과를 공통 응답 포맷(ApiResponse)으로 감싸서 반환한다.
         *
         * 요청 정보
         * - @RequestAttribute("userId") String userId
         *   : JwtAuthFilter에서 토큰을 검증한 뒤 request.setAttribute("userId", ...)로 설정한 인증 사용자 ID
         * - @RequestBody BookCreateRequest req
         *   : 클라이언트가 전송한 도서 등록 데이터 (title, description, content, categoryId)
         *
         * 응답 형식 (ResponseEntity<ApiResponse<?>>)
         * - 201: 등록 성공
         * - 400 필수 값 누락, 유효하지 않은 내용 등으로 IllegalArgumentException 발생 시 (잘못된 요청 데이터)
         * - 401 사용자 미존재, 카테고리 미존재 등 RuntimeException 발생 시 (인증/조회 관련 문제)
         * - 500: 위에서 처리하지 못한 예외가 발생한 경우 (서버 내부 오류)
         */
        log.info("도서 등록 요청: userId={}, title={}", userId, req.getTitle());

        BookCreateResponse data = bookService.createBook(userId, req);

        log.info("도서 등록 성공: bookId={}", data.getBookId());

        return ResponseEntity.status(201).body(
                new ApiResponse<>("success", "도서등록완료", data)
        );
    }

    @PutMapping("/update/{bookId}")
    public ResponseEntity<ApiResponse<?>> updateBook(
            @RequestAttribute("userId") String userId,
            @PathVariable Long bookId,
            @RequestBody BookUpdateRequest req
    ) {
        /**
         * 도서 수정 API (PUT)
         *
         * <동작 개요>
         * - JWT 인증된 사용자가 URL 경로로 전달된 bookId와 요청 본문의 수정 데이터를 기반으로 본인이 등록한 도서 정보를 검증 후 수정한다.
         *
         * <요청 정보>
         * - URL: /api/books/{bookId}
         * - @RequestAttribute("userId") String userId
         *   : JWT 토큰에서 추출된 인증 사용자 ID
         * - @PathVariable Long bookId
         *   : 수정 대상이 되는 도서의 고유 식별자(PK)
         * - @RequestBody BookUpdateRequest req
         *   : 수정할 도서 정보 (title, description, content, categoryId)
         *
         * <응답 형식>
         * - 200: 성공 (200 OK)
         * - 400: 실패 필수 입력 값 누락, 잘못된 요청 데이터인 경우 (잘못된 요청)
         * - 401: 실패 사용자 정보가 없거나, 존재하지 않는 도서를 수정하려 하거나, 본인이 등록하지 않은 도서를 수정하려 할 경우 (인증/조회/권한 문제)
         * - 500: 그 외 예기치 못한 서버 내부 오류 발생 시 (서버 오류)
         */
        BookUpdateResponse data = bookService.updateBook(userId, bookId, req);

        log.info("도서 수정 성공: bookId={}", data.getBookId());

        return ResponseEntity.ok(
                new ApiResponse<>("success", "도서수정완료", data)
        );
    }

    @DeleteMapping("/delete/{bookId}")
    public ResponseEntity<ApiResponse<DeleteBookResponse>> deleteBook(
            @RequestAttribute("userId") String userId,   // ★ 토큰에서 꺼낸 유저 ID
            @PathVariable Long bookId,
            @RequestBody(required = false) DeleteBookRequest body
    ) {
        /**
         * 도서 삭제 API (DELETE)
         *
         * <동작 개요>
         * - 인증된 사용자가 요청한 도서 ID를 기준으로 해당 도서를 삭제하고, 그 결과를 응답으로 반환한다.
         *
         * 요청 정보
         * - @RequestAttribute("userId") String userId
         *   : JwtAuthFilter에서 토큰을 검증한 뒤 request.setAttribute("userId", ...)로 설정한 인증 사용자 ID
         * - @PathVariable Long bookId
         *   : 삭제할 도서의 고유 식별자(ID)
         * - @RequestBody(required = false) DeleteBookRequest body
         *   : (선택) 삭제 대상 도서 ID를 포함하는 요청 바디
         *
         * 응답 형식 (ResponseEntity<ApiResponse<DeleteBookResponse>>)
         * - 200: 도서 삭제 성공
         * - 404: 해당 ID의 도서가 존재하지 않을 경우 (IllegalArgumentException 발생)
         * - 500: 그 외 서버 내부 오류 발생 시
         */
        log.info("도서 삭제 요청: path bookId={}", bookId);

        if (body != null && body.getBookId() != null
                && !body.getBookId().equals(bookId)) {
            log.warn("삭제 요청 bookId 불일치: path={}, body={}", bookId, body.getBookId());
        }

        DeleteBookResponse result = bookService.deleteBook(userId, bookId);

        log.info("도서 삭제 성공: bookId={}", bookId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "도서삭제성공",
                        result
                )
        );
    }

}
