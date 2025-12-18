package com.example.back.service;

import com.example.back.DTO.CategoryItemResponse;
import com.example.back.entity.Category;
import com.example.back.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryItemResponse> getCategories() {
        /**
         * 카테고리 목록 조회 서비스
         *
         * <동작 개요>
         * - 데이터베이스에 저장된 모든 카테고리 정보를 ID 기준 오름차순으로 조회한 후,
         *   클라이언트에 전달할 CategoryItemResponse DTO 형태로 변환하여 반환한다.
         *
         * 처리 흐름
         * 1. CategoryRepository를 통해 전체 카테고리 목록을 ID 오름차순으로 조회한다.
         * 2. 조회한 Category 엔티티 목록을 CategoryItemResponse DTO 리스트로 변환한다.
         * 3. 변환된 DTO 리스트를 컨트롤러로 반환한다.
         *
         * @return List<CategoryItemResponse>
         *  - 카테고리 ID와 이름을 포함한 카테고리 목록 데이터
         */

        // 1) DB에서 전체 카테고리 조회
        List<Category> categories =
                categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        // 2) Entity -> DTO 변환
        return categories.stream()
                .map(c -> new CategoryItemResponse(
                        c.getId(),   // categoryId
                        c.getName()  // name
                ))
                .toList();
    }
}