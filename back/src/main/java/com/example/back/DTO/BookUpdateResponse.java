package com.example.back.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookUpdateResponse {
    private Long bookId;

    public BookUpdateResponse(Long bookId) {
        this.bookId = bookId;
    }
}
