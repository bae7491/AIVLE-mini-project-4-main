package com.example.back.DTO;

import com.example.back.entity.Book;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class BookListResponse {

    private int page;
    private int totalPages;
    private long totalItems;
    private List<BookItem> books;

    public BookListResponse(int page, int totalPages, long totalItems, List<BookItem> books) {
        this.page = page;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.books = books;
    }

    // ✅ 내부에서만 사용하는 Book DTO (외부 파일 X)
    @Getter
    @NoArgsConstructor
    public static class BookItem {

        private Long bookId;
        private String title;
        private String category;
        private String imageUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public BookItem(Book book) {
            this.bookId = book.getId();
            this.title = book.getTitle();
            this.category = book.getCategoryId().getName();
            this.imageUrl = book.getImageUrl();
            this.createdAt = book.getCreated_at();
            this.updatedAt = book.getUpdated_at();
        }
    }

    // ✅ Page<Book> → BookListResponse 변환 팩토리
    public static BookListResponse from(Page<Book> pageResult) {
        return new BookListResponse(
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                pageResult.getContent().stream()
                        .map(BookItem::new)
                        .toList()
        );
    }
}