package com.example.back.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 도서 삭제 성공 시 클라이언트에게 돌려줄 응답 DTO
 *  - bookId  : 삭제 요청한 도서 ID
 *  - deleted : 실제로 삭제된 행 수(성공이면 1, 실패/미삭제면 0)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteBookResponse {

    private Long bookId;  // 삭제된 도서 ID
    private int deleted;  // 삭제된 행 수(1이면 정상 삭제)
}