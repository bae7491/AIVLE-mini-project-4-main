package com.example.back.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.back.DTO.ApiResponse;
import com.example.back.DTO.DeleteRequest;
import com.example.back.DTO.LoginRequest;
import com.example.back.DTO.LoginResponse;
import com.example.back.DTO.SignupRequest;
import com.example.back.DTO.UpdateRequest;
import com.example.back.entity.User;
import com.example.back.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService; 
    
    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;
     
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(
            @RequestHeader(value = "API-KEY", required = false) String apiKey,
            @RequestBody SignupRequest req
    ) {
        /**
         * 회원가입 API
         * - 클라이언트가 보낸 회원가입 정보(JSON)를 받아서 서비스로 전달하고 결과를 응답합니다.
         *
         * @param API-KEY 헤더 값
         *
         * @param req SignupRequest
         *   - 클라이언트가 보낸 JSON:
         *       {
         *         "id": "사용자ID",
         *         "pw": "비밀번호",
         *         "name": "이름"
         *       }
         *
         * @return ResponseEntity<ApiResponse<?>>
         *   - 성공/실패 여부를 나타내는 공통 응답 형태
         *   - 200: 회원가입 성공
         *   - 401: 잘못된 요청 (중복 ID 등)
         *   - 500: 서버 내부 오류
         */
        log.info("회원가입 요청: id={}, name={}, apiKey={}", req.getId(), req.getName(), apiKey);

        authService.signup(req, apiKey);

        log.info("회원가입 완료: id={}", req.getId());
        return ResponseEntity.ok(
            new ApiResponse<>("success", "회원가입 성공", null)
        );
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        /**
         * 로그인 API
         * - 인증 성공 시 Service에서 JWT 토큰을 생성하여 반환받고,
         *   Controller에서 헤더 + Body(LoginResponse) 조립 후 반환합니다.
         *
         * @param req LoginRequest
         *   - 클라이언트가 보낸 JSON:
         *       {
         *         "id": "사용자ID",
         *         "pw": "비밀번호"
         *       }
         *
         * @return ResponseEntity<?>
         *   - 200 OK: Authorization 헤더 + userId 반환
         *   - 404 NOT FOUND: 아이디 없음
         *   - 401 UNAUTHORIZED: 비밀번호 불일치
         *   - 500 INTERNAL SERVER ERROR: 서버 오류
         */
        log.info("로그인 요청: id={}", req.getId());

        // 모든 예외는 GlobalExceptionHandler에서 처리됨
        Map<String, String> tokens = authService.login(req);

        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        long refreshExpirationSeconds = refreshExpirationMs / 1000;

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(refreshExpirationSeconds)
            .build();

        LoginResponse response = new LoginResponse(
            "success",
            "로그인 성공",
            req.getId()
        );

        log.info("로그인 성공: id={}", req.getId());

        return ResponseEntity.ok()
            .header("Authorization", "Bearer " + accessToken)
            .header("Set-Cookie", refreshCookie.toString())
            .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletResponse response
    ) {
        /**
         * 로그아웃 API
         * - Authorization 헤더에 전달된 JWT 토큰으로 로그아웃 처리합니다.
         *
         * @param authHeader Authorization 헤더 값
         *   예: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
         *
         * @return ResponseEntity<?>
         *   - 200: 로그아웃 성공
         *   - 401: 잘못된 토큰 또는 로그아웃 불가
         *   - 500: 서버 오류
         */
        String token = "";

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // "Bearer " 이후 토큰만 추출
        }
        log.info("로그아웃 요청: token={}", token);

        authService.logout(token);   
        
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);       
        refreshCookie.setPath("/");           
        refreshCookie.setMaxAge(0);
        refreshCookie.setAttribute("SameSite", "Lax");

        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new ApiResponse<>("success", "로그아웃 완료", null));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "API-KEY", required = false) String apiKey, 
            @RequestBody UpdateRequest req
    ) {
        /**
         * 회원정보 수정 API
         *
         * 처리 흐름:
         * 1) Authorization 헤더에서 Access Token 추출
         * 2) 토큰에서 userId 추출 및 유효성 검증
         * 3) name, pw 중 전달된 필드만 수정
         * 4) DB 업데이트 후 성공 응답 반환
         *
         * Request Header:
         *   Authorization: Bearer JWT_ACCESS_TOKEN
         *   API-KEY: apikey 값
         *
         * Request Body JSON:
         *   {
         *     "name": "새 이름",
         *     "pw": "새 비밀번호"
         *   }
         */

        log.info("회원정보 수정 요청");
        String token = authHeader.replace("Bearer ", "");

        // 서비스에 업데이트 요청
        authService.updateUser(token, req, apiKey);

        log.info("회원정보 수정 완료");

        return ResponseEntity.ok(
            new ApiResponse<>("success", "회원정보가 성공적으로 수정되었습니다.", null)
        );    
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody DeleteRequest req,
            HttpServletResponse response) {
        /**
         * 회원 탈퇴 API
         * - Authorization 헤더에서 JWT 토큰 추출
         * - 토큰의 사용자와 req.pw를 검증하여 DB에서 삭제
         *
         * @param req DeleteRequest
         *   - 클라이언트가 보낸 JSON:
         *       {
         *         "pw": "사용자 비밀번호"
         *       }
         * 
         * @return ResponseEntity<?>
         *   - 200: 탈퇴 성공
         *   - 401: 토큰 또는 비밀번호가 올바르지 않음
         *   - 500: 서버 내부 오류
         */
        log.info("회원 탈퇴 요청: AuthorizationHeader={}", authorizationHeader);

        String token = authorizationHeader.replace("Bearer ", "");
        log.info("토큰 추출 완료: {}", token);

        // 서비스 호출
        authService.deleteUser(token, req.getPw());

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);       
        refreshCookie.setPath("/");           
        refreshCookie.setMaxAge(0);
        refreshCookie.setAttribute("SameSite", "Lax");

        response.addCookie(refreshCookie);

        log.info("회원 탈퇴 완료");

        // 성공 응답
        return ResponseEntity.ok(
                new ApiResponse<>("success", "회원 탈퇴 완료", null)
        );
    }


    @PostMapping("/token/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        /**
         * 액세스 토큰 유효성 검사 API
         * - Authorization 헤더에서 JWT 토큰을 추출하여 검증
         *
         * 헤더 예시:
         *   Authorization: Bearer JWT_ACCESS_TOKEN
         */

        String token = authorizationHeader.replace("Bearer ", "");

        log.info("토큰 유효성 검사 요청: token={}", token);

        authService.validateAccessToken(token);

        return ResponseEntity.ok(
            new ApiResponse<>("success", "유효한 토큰입니다.", null)
        );
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        /**
         * 액세스 토큰 재발급 API
         * - 쿠키로 전달된 Refresh Token을 이용하여
         *   새로운 Access Token을 발급합니다.
         *
         * 처리 흐름:
         *   1) refreshToken 쿠키 존재 여부 확인
         *   2) 유효성 검사
         *   3) 서버에 저장된 refreshToken과 일치 여부 확인
         *   4) 새 AccessToken 생성 후 Authorization 헤더로 응답
         */

        log.info("액세스 토큰 재발급 요청: refreshToken={}", refreshToken);
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("토큰 재발급 실패 - refreshToken 쿠키 없음");
            throw new IllegalArgumentException("Refresh Token이 전송되지 않았습니다.");
        }

        String newAccessToken = authService.reissueAccessToken(refreshToken);
        log.info("액세스 토큰 재발급 완료");

        return ResponseEntity.ok()
            .header("Authorization", "Bearer " + newAccessToken)
            .body(new ApiResponse<>("success", "액세스 토큰 재발급 성공", null));
    }

    @GetMapping("/api-key")
    public ResponseEntity<?> getUserApiKey(
            @RequestHeader("Authorization") String authHeader
    ) {
        /**
         * API Key 조회 API
         * - Authorization 헤더로 전달된 Access Token을 통해
         *   사용자 ID를 추출하고, 해당 사용자의 등록된 API Key를 반환합니다.
         *
         * 처리 흐름:
         *   1) Authorization 헤더 유효성 검사
         *   2) Access Token 추출
         *   3) Service 호출하여 API Key 조회
         *   4) 조회 성공 시 API Key 반환
         *
         * 예외 처리:
         *   - 잘못된 토큰: IllegalArgumentException -> 401
         *   - 사용자 없음 / API Key 없음: RuntimeException 또는 IllegalArgumentException -> GlobalHandler 처리
         */
        log.info("API Key 조회 요청");

        String token = authHeader.replace("Bearer ", "");
        log.info("API Key 조회 - Access Token 추출 완료");

        // 서비스 호출
        String apiKey = authService.getUserApiKey(token);
        log.info("API Key 조회 완료: apiKey 존재 여부={}", (apiKey != null));

        return ResponseEntity.ok(
                new ApiResponse<>("success", "API Key 조회 성공", apiKey)
        );
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(
            @RequestHeader("Authorization") String authHeader
    ) {
        /**
         * 사용자 정보 조회 API
         * - Authorization 헤더에서 JWT 토큰을 추출하여 사용자 인증
         * - 토큰에서 userId를 얻고 해당 사용자의 정보를 조회하여 반환
         *
         * @param authHeader Authorization 헤더 값
         *   - 예: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
         *
         * @return ResponseEntity<?>
         *   - 200: 사용자 정보 조회 성공
         *   - 401: 토큰이 유효하지 않음 (만료, 변조 등)
         *   - 404: 해당 사용자 정보 없음
         *   - 500: 서버 내부 오류
         */
        log.info("사용자 정보 조회 요청: AuthorizationHeader={}", authHeader);

        // Authorization 헤더 검증
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("사용자 정보 조회 실패 - Authorization 헤더가 유효하지 않음: {}", authHeader);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        // "Bearer " 제거 후 토큰만 추출
        String token = authHeader.substring(7);
        log.info("토큰 추출 완료: {}", token);

        // 서비스 호출: 토큰 기반 사용자 정보 조회
        User user = authService.getUserInfo(token);
        log.info("사용자 정보 조회 성공: id={}, name={}", user.getId(), user.getName());

        // 응답 헤더에 API Key 추가 
        HttpHeaders headers = new HttpHeaders();
        if (user.getApiKey() != null) {
            headers.add("API-KEY", user.getApiKey());
            log.info("API Key 헤더에 추가 완료: apiKey={}", user.getApiKey());
        } else {
            log.info("API Key 없음: id={}", user.getId());
        }

        // 응답 Body 구성
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("message", "사용자 정보 조회 성공");
        body.put("id", user.getId());
        body.put("name", user.getName());

        log.info("사용자 정보 조회 API 응답 반환 완료: id={}", user.getId());

        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}   

