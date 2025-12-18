package com.example.back.service;

import com.example.back.DTO.*;
import com.example.back.entity.Book;
import com.example.back.entity.Category;
import com.example.back.entity.User;

import com.example.back.repository.BookRepository;
import com.example.back.repository.CategoryRepository;
import com.example.back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BookCoverStorageService bookCoverStorageService;

    public BookListResponse getBooks(int page, int size) {
        /**
         * 도서 목록 조회 서비스 로직 (GET)
         *
         * - 전달받은 page, size 값을 기반으로 Pageable 객체를 생성하여 페이징 처리된 도서 목록을 조회한다.
         * - JPA의 findAll(Pageable)을 사용하여 전체 도서 목록을 페이지 단위로 조회한다.
         * - 조회된 Page<Book> 객체를 BookListResponse DTO 형태로 변환하여 반환한다.
         *
         * @param page int
         *     - 조회할 페이지 번호 (0부터 시작)
         * @param size int
         *     - 한 페이지에 조회할 도서 개수
         *
         * @return BookListResponse
         *     - 현재 페이지 번호 (page)
         *     - 전체 페이지 수 (totalPages)
         *     - 도서 목록 리스트 (books)
         */
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> result = bookRepository.findAll(pageable);

        // Page<Book> -> BookListResponse 로 변환
        return BookListResponse.from(result);
    }

    public BookListResponse searchBooksByTitle(String title, int page, int size) {
        /**
         * 도서 제목으로 검색 (부분 일치, 대소문자 구분 없음)
         *
         * @param title 검색할 제목 키워드
         * @param page  0부터 시작하는 페이지 인덱스
         * @param size  페이지당 조회 수
         * @return BookListResponse (페이지 정보 + 도서 목록)
         */
        log.info("도서 제목 검색 서비스 시작: title={}, page={}, size={}", title, page, size);

        if (title == null || title.isBlank()) {
            log.warn("도서 제목 검색 실패 - 잘못된 검색어: title 비어 있음");
            throw new IllegalArgumentException("검색어(title)가 올바르지 않습니다.");
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "id"); // 최신 순 정렬
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> result = bookRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);

        log.info("도서 제목 검색 서비스 완료: title={}, totalElements={}", title, result.getTotalElements());

        return BookListResponse.from(result);
    }

    public BookDetailResponse getBookDetail(Long bookId) {
        /**
         * 도서 상세 조회 (GET)
         *
         * @param bookId 조회할 도서의 고유 ID
         * @return 조회된 도서 정보를 담은 BookDetailResponse DTO
         *
         * - 전달받은 bookId를 기준으로 DB에서 도서 엔티티를 조회한다.
         * - 해당 ID의 도서가 존재하지 않을 경우 IllegalArgumentException을 발생시킨다.
         * - 조회된 Book 엔티티를 BookDetailResponse DTO로 변환하여 반환한다.
         */
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다."));

        return BookDetailResponse.from(book);
    }

    @Transactional
    @SuppressWarnings("null")
    public BookCreateResponse createBook(String userId, BookCreateRequest req) {
        /**
         * 도서 등록 서비스 로직 (POST)
         *
         * <동작 개요>
         * - JWT 인증을 통해 전달된 userId와 클라이언트가 보낸 도서 정보를 바탕으로
         *   새로운 Book 엔티티를 생성하고 DB에 저장한 뒤, 생성된 도서의 PK(bookId)만 반환한다.
         *
         * @param userId JwtAuthFilter에서 추출된 인증 사용자 ID (users.id)
         * @param req    도서 등록 요청 본문 데이터 (title, description, content, categoryId)
         *
         * @return BookCreateResponse
         *     - 생성된 도서의 PK 값(bookId)을 담은 응답 DTO
         *
         * @throws IllegalArgumentException
         *     - 제목/설명/내용 등 필수 값이 비어 있거나 잘못된 경우
         *
         * @throws RuntimeException
         *     - userId에 해당하는 사용자 정보를 찾지 못한 경우
         *     - categoryId에 해당하는 카테고리 정보를 찾지 못한 경우
         */
        log.info("도서 등록 서비스 시작: userId={}, title={}", userId, req.getTitle());

        // 1) 필수 값 검증
        if (req.getTitle() == null || req.getTitle().isBlank()
                || req.getDescription() == null || req.getDescription().isBlank()
                || req.getContent() == null || req.getContent().isBlank()) {
            log.warn("도서 등록 실패 - 잘못된 요청 데이터: title/description/content 비어 있음");
            throw new IllegalArgumentException("도서 정보가 올바르지 않습니다.");
        }

        // 2) 사용자 조회 (존재 여부 확인)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("도서 등록 실패 - 사용자 조회 실패: userId={}", userId);
                    return new RuntimeException("사용자 정보를 찾을 수 없습니다.");
                });

        // 3) 카테고리 조회
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("도서 등록 실패 - 카테고리 조회 실패: categoryId={}", req.getCategoryId());
                    return new RuntimeException("카테고리 정보를 찾을 수 없습니다.");
                });

        // 4) Book 엔티티 생성
        Book book = new Book();
        book.setUser(user);
        book.setCategoryId(category);
        book.setTitle(req.getTitle());
        book.setDescription(req.getDescription());
        book.setContent(req.getContent());

        // 5) 우선 Book 저장해서 bookId 확보
        Book saved = bookRepository.save(book);

        log.info("도서 등록 서비스 - Book 저장 완료: bookId={}", saved.getId());

        // 6) 이미지 URL이 요청에 들어온 경우에만 처리
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {

            String publicUrl = bookCoverStorageService.saveCoverFromUrl(
                    req.getImageUrl(),
                    saved.getId()
            );

            // 유효하지 않은 URL(403/404 등) → 도서 등록 자체를 막기
            if (publicUrl == null) {
                log.warn("도서 등록 실패 - 유효하지 않은 이미지 URL: {}", req.getImageUrl());
                // 트랜잭션 롤백 → 이미 저장된 Book도 함께 롤백됨
                throw new IllegalArgumentException("유효하지 않은 이미지 URL입니다.");
            }

            // 정상적으로 다운로드된 경우에만 이미지 URL 세팅
            saved.setImageUrl(publicUrl);
        }

        log.info("도서 등록 서비스 완료: bookId={}", saved.getId());

        return new BookCreateResponse(saved.getId());
    }

    @Transactional
    @SuppressWarnings("null")
    public BookUpdateResponse updateBook(String userId, Long bookId, BookUpdateRequest req) {
        /**
         * 도서 수정 서비스 로직
         *
         * <동작 개요>
         * - 인증된 사용자(userId)가 요청한 도서(bookId)에 대해
         *   입력받은 수정 정보(title, description, content, categoryId)를 검증한 후
         *   본인이 등록한 도서인지 여부를 확인하고, 문제가 없을 경우 도서 정보를 수정한다.
         *
         * @param userId 수정 요청을 한 인증 사용자 ID (JWT 토큰에서 추출된 값)
         * @param bookId 수정 대상 도서의 고유 식별자(PK)
         * @param req    도서 수정 요청 데이터 (title, description, content, categoryId)
         *
         * @return BookUpdateResponse
         *     - 수정 완료된 도서의 ID(bookId)를 담은 응답 DTO
         *
         * @throws IllegalArgumentException
         *     - 필수 입력 값이 누락되거나 잘못된 경우
         *
         * @throws RuntimeException
         *     - 사용자 정보를 찾지 못한 경우
         *     - 도서 정보를 찾지 못한 경우
         *     - 요청 사용자가 도서 소유자가 아닌 경우
         *     - 카테고리 정보를 찾지 못한 경우
         */
        log.info("도서 수정 서비스 시작: userId={}, bookId={}, title={}", userId, bookId, req.getTitle());

        // 1) 필수 값 검증
        if (req.getTitle() == null || req.getTitle().isBlank()
                || req.getDescription() == null || req.getDescription().isBlank()
                || req.getContent() == null || req.getContent().isBlank()) {
            log.warn("도서 수정 실패 - 잘못된 요청 데이터: title/description/content 비어 있음");
            throw new IllegalArgumentException("도서 정보가 올바르지 않습니다.");
        }

        // 2) 사용자 존재 여부 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("도서 수정 실패 - 사용자 조회 실패: userId={}", userId);
                    return new RuntimeException("사용자 정보를 찾을 수 없습니다.");
                });

        // 3) 수정 대상 도서 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.warn("도서 수정 실패 - 도서 조회 실패: bookId={}", bookId);
                    return new RuntimeException("도서 정보를 찾을 수 없습니다.");
                });

        // 4) 소유자 검증 (본인 글만 수정 가능)
        if (!book.getUser().getId().equals(user.getId())) {
            log.warn("도서 수정 실패 - 권한 없음: 요청 userId={}, 도서 소유자={}",
                    userId, book.getUser().getId());
            throw new RuntimeException("본인이 등록한 도서만 수정할 수 있습니다.");
        }

        // 5) 카테고리 조회
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("도서 수정 실패 - 카테고리 조회 실패: categoryId={}", req.getCategoryId());
                    return new RuntimeException("카테고리 정보를 찾을 수 없습니다.");
                });

        // 6) 기본 필드 수정
        book.setCategoryId(category);
        book.setTitle(req.getTitle());
        book.setDescription(req.getDescription());
        book.setContent(req.getContent());
        book.setUpdated_at(java.time.LocalDateTime.now());

        // 7) 우선 기본 정보 저장
        Book saved = bookRepository.save(book);
        log.info("도서 수정 기본 정보 저장 완료: bookId={}, imageUrl(초기)={}",
                saved.getId(), saved.getImageUrl());

        // 8) 이미지 URL이 요청에 들어온 경우에만 처리 (등록과 동일)
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {

            String publicUrl = bookCoverStorageService.saveCoverFromUrl(
                    req.getImageUrl(),
                    saved.getId()
            );

            // 유효하지 않은 URL(403/404 등) → 수정 자체를 막기 (트랜잭션 롤백)
            if (publicUrl == null) {
                log.warn("도서 수정 실패 - 유효하지 않은 이미지 URL: {}", req.getImageUrl());
                throw new IllegalArgumentException("유효하지 않은 이미지 URL입니다.");
            }

            // 정상적으로 다운로드된 경우에만 이미지 URL 세팅
            saved.setImageUrl(publicUrl);
        }

        log.info("도서 수정 서비스 완료: bookId={}, 최종 imageUrl={}",
                saved.getId(), saved.getImageUrl());

        return new BookUpdateResponse(saved.getId());
    }

    @Transactional
    public DeleteBookResponse deleteBook(String userId, Long bookId) {
        /**
         * 도서 삭제 서비스 로직 (DELETE)
         *
         * @param userId 삭제 요청을 보낸 사용자 ID
         * @param bookId 삭제할 도서의 고유 ID
         * @return 삭제된 도서 정보를 담은 DeleteBookResponse
         *
         * - 도서 ID를 기준으로 삭제 대상 도서를 조회한다.
         * - 도서가 존재하지 않을 경우 IllegalArgumentException을 발생시킨다.
         * - 요청한 사용자와 도서 작성자가 동일한지 검증하여,
         *   본인이 작성한 도서만 삭제할 수 있도록 권한을 체크한다.
         * - 권한이 검증되면 해당 도서를 DB에서 삭제한다.
         * - 삭제 결과로 삭제된 도서 ID와 삭제 성공 여부를 반환한다.
         */

        // 1) 도서 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다."));

        // 2) 소유자 체크 (작성자와 요청한 유저가 같은지)
        if (!book.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인이 등록한 도서만 삭제할 수 있습니다.");
        }

        // 3) 삭제 진행
        bookRepository.delete(book);

        // 4) 결과 반환
        return new DeleteBookResponse(bookId, 1);
    }

}
