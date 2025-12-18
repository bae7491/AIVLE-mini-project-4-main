package com.example.back.controller;

import com.example.back.DTO.ApiResponse;
import com.example.back.DTO.CategoryItemResponse;
import com.example.back.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories") // 카테고리 관련 API 기본 경로
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryItemResponse>>> getCategories() {
        /**
         * 카테고리 목록 조회 API (GET)
         *
         * <동작 개요>
         * - 시스템에 등록된 전체 카테고리 목록을 조회하여 클라이언트에게 반환한다.
         *
         * 요청 정보
         * - 별도의 요청 파라미터 없음
         *
         * 응답 형식 (ResponseEntity<ApiResponse<List<CategoryItemResponse>>>)
         * - 200: 카테고리 목록 조회 성공
         * - 500: 서버 내부 오류 발생 시
         */

        log.info("카테고리 목록 조회 요청");

        List<CategoryItemResponse> data = categoryService.getCategories();

        log.info("카테고리 목록 조회 성공: size={}", data.size());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "success",
                        "카테고리목록조회성공",
                        data
                )
        );
    }
}