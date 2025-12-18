package com.example.back.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프론트 드롭다운에 보여줄 카테고리 한 개 정보 DTO
 *  - categoryId : 카테고리 PK
 *  - name      : 화면에 보여줄 이름
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryItemResponse {

    private Long categoryId; // 카테고리 ID
    private String name;     // 카테고리 이름
}