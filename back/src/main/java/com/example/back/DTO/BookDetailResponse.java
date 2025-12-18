package com.example.back.DTO;

import com.example.back.entity.Book;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BookDetailResponse {

    private Long bookId;
    private String title;
    private String description;
    private String content;

    private Long categoryId;
    private String imageUrl;
    private String ownerUser;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public BookDetailResponse(Long bookId,
                              String title,
                              String description,
                              String content,
                              Long categoryId,
                              String imageUrl,
                              String ownerUser,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.bookId = bookId;
        this.title = title;
        this.description = description;
        this.content = content;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.ownerUser = ownerUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BookDetailResponse from(Book book) {
        return BookDetailResponse.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .content(book.getContent())
                .categoryId(book.getCategoryId().getId())
                .imageUrl(book.getImageUrl())
                .ownerUser(book.getUser().getId())
                .createdAt(book.getCreated_at())
                .updatedAt(book.getUpdated_at())
                .build();
    }
}
