package com.example.back.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.back.repository.CategoryRepository;
import com.example.back.entity.Category;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(1)
@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {

        long count = categoryRepository.count();

        if (count > 0) {
            log.info("카테고리 초기화 생략됨 — 기존 데이터 {}건 존재", count);
            return;
        }

        List<String> categories = List.of(
            // ===== 보편적 카테고리 =====
            "일상",
            "취미",
            "교육",
            "비즈니스",
            "경제",
            "예술",
            "라이프스타일",
            "건강",
            "여행",

            // ===== 기술 관련 카테고리 =====
            "기술",
            "과학",
            "프로그래밍",
            "인공지능 / 머신러닝",
            "웹 개발",
            "모바일 개발",
            "데이터베이스",
            "클라우드 / 데브옵스",
            "보안",
            "게임 개발"
        );

        categories.forEach(name -> categoryRepository.save(new Category(name)));

        log.info("기본 카테고리 {}건 초기화 완료", categories.size());
    }
}
