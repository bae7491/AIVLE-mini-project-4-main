package com.example.back.DTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
/**
 * 도서 삭제 요청 바디 DTO
 *  - JSON 에서 전달되는 bookId 를 받는다.
 */
@Getter
@NoArgsConstructor
public class DeleteBookRequest { // ← 이름 바꿔서 새로 사용
    private Long bookId; // 삭제할 도서 ID
}

