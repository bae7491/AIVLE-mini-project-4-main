package com.example.back.exception;

import io.jsonwebtoken.*;

import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.back.DTO.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ====== 400 Bad Request ======
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({MalformedJwtException.class, UnsupportedJwtException.class})
    public ResponseEntity<?> handleMalformedJwt(Exception e) {
        return build(HttpStatus.BAD_REQUEST, "잘못된 JWT 형식입니다.");
    }


    // ====== 401 Unauthorized ======
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredToken(ExpiredJwtException e) {
        return build(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.");
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleInvalidJwt(SecurityException e) {
        return build(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException e) {

        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());

        String message = Optional.ofNullable(e.getReason())
                .filter(reason -> !reason.isBlank())
                .orElse("오류가 발생했습니다.");

        return build(status, message);
    }

    // ====== 500 Database Error ======
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDatabase(DataAccessException e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 처리 중 오류가 발생했습니다.");
    }


    // ====== 500 Internal Error ======
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        
        // e.getMessage()가 null이거나 빈 문자열이면 기본 메시지 사용
        String message = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : "처리 중 오류가 발생했습니다.";

        return build(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }

    // 공통 응답 생성 함수
    private ResponseEntity<?> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>("error", message, null));
    }
}
