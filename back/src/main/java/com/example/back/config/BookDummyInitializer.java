package com.example.back.config;

import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.back.entity.Book;
import com.example.back.entity.Category;
import com.example.back.entity.User;
import com.example.back.repository.BookRepository;
import com.example.back.repository.CategoryRepository;
import com.example.back.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(2)
@Component
public class BookDummyInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public BookDummyInitializer(
            BookRepository bookRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        log.info("=== BookDummyInitializer 실행 시작 ===");

        // 이미 데이터 존재 시 중단
        long existingCount = bookRepository.count();
        if (existingCount > 0) {
            log.info("Book 더미 데이터 {}건 존재 — 초기화 생략", existingCount);
            return;
        }

        // ===================================================================
        // 1) admin 유저 조회 or 생성
        // ===================================================================
        log.info("admin 유저 존재 여부 확인 중...");
        User admin = userRepository.findById("admin").orElse(null);

        if (admin == null) {
            log.warn("admin 계정을 찾을 수 없음 → 자동 생성 진행");

            admin = new User();
            admin.setId("admin");
            admin.setName("관리자");
            admin.setPw(passwordEncoder.encode("admin"));

            userRepository.save(admin);
            log.info("admin 계정 생성 완료");
        } else {
            log.info("admin 계정 존재 확인 완료");
        }

        // ===================================================================
        // 2) 카테고리 목록 조회
        // ===================================================================
        log.info("카테고리 목록 조회 중...");
        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            log.error("카테고리 데이터가 0건입니다. Book 더미 데이터 생성 중단.");
            return;
        }

        log.info("카테고리 {}건 로딩 완료", categories.size());

        Random random = new Random();

        // ===================================================================
        // 3) 더미 Book 데이터 생성
        // ===================================================================
        int size = 100;
        log.info("Book 더미 데이터 생성 시작... 총 {}건 예정", size);

        // 카테고리 ID 목록만 추출
        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();

        for (int i = 1; i <= size; i++) {

            // 1) 랜덤 category_id 선택
            Long randomCategoryId = categoryIds.get(random.nextInt(categoryIds.size()));

            // 2) category_id로 실제 Category 엔티티 조회
            Category randomCategory = categoryRepository.findById(randomCategoryId)
                    .orElseThrow(() -> new IllegalStateException("Category 조회 실패: id=" + randomCategoryId));

            // log.info("Book {}번 -> 선택된 카테고리 ID={}, name={}", i, randomCategory.getId(), randomCategory.getName());

            // 3) Book 생성
            Book book = new Book();
            book.setUser(admin);
            book.setCategoryId(randomCategory);  // Category 엔티티 설정
            book.setTitle("더미 책 제목 " + i);
            book.setDescription("이것은 더미 책 설명입니다.");
            book.setContent("이것은 더미 책 내용입니다. " + i + "번째 더미 데이터입니다.");
            book.setImageUrl("https://picsum.photos/200/320?random=" + i);

            bookRepository.save(book);

            if (i % 10 == 0) {
                log.info("더미 Book {}건 생성 완료...", i);
            }
        }

        log.info("Book 더미 데이터 {}건 삽입 완료", size);
        log.info("=== BookDummyInitializer 실행 종료 ===");

    }
}
