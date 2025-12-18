package com.example.back.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookCreateRequest {
    private String title;
    private String description;
    private String content;
    private Long categoryId;
    private String imageUrl;
}
