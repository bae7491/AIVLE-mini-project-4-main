package com.example.back.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;    // success or error
    private String message;   // 메시지
    private T data;           // 데이터(필요 없으면 null)
}