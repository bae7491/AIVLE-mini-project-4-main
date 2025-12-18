package com.example.back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.back.DTO.LoginRequest;
import com.example.back.DTO.SignupRequest;
import com.example.back.DTO.UpdateRequest;
import com.example.back.entity.RefreshToken;
import com.example.back.entity.User;
import com.example.back.jwt.JwtUtil;
import com.example.back.repository.RefreshTokenRepository;
import com.example.back.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest req, String apiKey) {
        /**
         * 회원가입 서비스 로직
         * - 중복 ID 여부 확인
         * - User 엔티티 생성 및 저장
         *
         * @param req SignupRequest
         *   - req.getId(): 사용자 ID
         *   - req.getPw(): 비밀번호
         *   - req.getName(): 이름
         */
        log.info("회원가입 처리 시작: id={}", req.getId());
        if (req.getId() == null || req.getId().isBlank()) {
            throw new IllegalArgumentException("회원가입 실패 - 아이디는 필수 입력값입니다.");
        }

        if (req.getPw() == null || req.getPw().isBlank()) {
            throw new IllegalArgumentException("회원가입 실패 - 비밀번호는 필수 입력값입니다.");
        }

        if (userRepository.existsById(req.getId())) {
            log.warn("회원가입 실패 - 이미 존재하는 아이디: id={}", req.getId());
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = new User();
        user.setId(req.getId());
        user.setPw(passwordEncoder.encode(req.getPw()));
        user.setName(req.getName());
        // API Key 저장 (null 허용)
        if (apiKey != null && !apiKey.isBlank()) {
            user.setApiKey(apiKey);
            log.info("API Key 저장 완료: id={}", req.getId());
        } else {
            log.info("API Key 없음: id={}", req.getId());
        }

        userRepository.save(user);

        log.info("회원가입 처리 완료: id={}", req.getId());
    }

    public Map<String, String> login(LoginRequest req) {
        /**
         * 로그인 서비스 로직
         *
         * - 사용자 ID로 User 엔티티 조회
         * - 비밀번호 검증 (BCrypt 해시 비교)
         * - Access Token 및 Refresh Token 생성
         * - Refresh Token을 DB에 저장
         * - 생성된 토큰(access/refresh)을 Map 형태로 반환
         *
         * 처리 흐름:
         *   1) 사용자 존재 여부 검사
         *   2) 비밀번호 일치 여부 확인
         *   3) Access Token 생성
         *   4) Refresh Token 생성 및 만료시간 설정
         *   5) Refresh Token DB 저장 (insert or update)
         *   6) 최종적으로 두 토큰을 반환(Map)
         *
         * @param req LoginRequest
         *      - req.getId(): 로그인 ID
         *      - req.getPw(): 입력한 비밀번호
         *
         * @return Map<String, String>
         *      - accessToken: JWT Access Token
         *      - refreshToken: JWT Refresh Token
         */
        log.info("로그인 처리 시작: id={}", req.getId());

        if (req.getId() == null || req.getId().isBlank()) {
            throw new IllegalArgumentException("로그인 실패 - 아이디는 필수 입력값입니다.");
        }
        if (req.getPw() == null || req.getPw().isBlank()) {
            throw new IllegalArgumentException("로그인 실패 - 비밀번호는 필수 입력값입니다.");
        }

        User user = userRepository.findById(req.getId())
            .orElseThrow(() -> {
                log.warn("로그인 실패 - 존재하지 않는 아이디: id={}", req.getId());
                return new IllegalArgumentException("등록되지 않은 아이디입니다.");
            });

        // 2) 비밀번호 불일치
        if (!passwordEncoder.matches(req.getPw(), user.getPw())) {
            log.warn("로그인 실패 - 비밀번호 불일치: id={}", req.getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        // 3) 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());
        Long expiry = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000);

        // 4) Refresh Token 저장 처리
        try {
            RefreshToken savedToken = refreshTokenRepository.findByUserId(user.getId()).orElse(null);

            if (savedToken == null) {
                savedToken = new RefreshToken(user, refreshToken, expiry);
                log.info("Refresh Token 신규 생성: userId={}", user.getId());
            } else {
                savedToken.updateToken(refreshToken, expiry);
                log.info("기존 Refresh Token 업데이트: userId={}", user.getId());
            }

            refreshTokenRepository.save(savedToken);
            log.info("Refresh Token 저장 완료: userId={}", user.getId());

        } catch (DataAccessException e) {
            log.error("Refresh Token 저장 실패(DB 오류): userId={}, error={}", user.getId(), e.getMessage());
            throw new RuntimeException("서버 DB 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("Refresh Token 저장 실패(기타 오류): userId={}, error={}", user.getId(), e.toString());
            throw new RuntimeException("리프레시 토큰 저장 중 오류가 발생했습니다.");
        }

        // 5) 결과 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        log.info("로그인 성공 - 토큰 발급 완료: id={}", req.getId());

        return tokens;
    }
    
    @Transactional
    public void logout(String token) {
        /**
         * 로그아웃 서비스 로직
         *
         * - Access Token에서 userId 추출
         * - 저장된 Refresh Token 삭제
         * - Access Token은 클라이언트 단에서 제거됨
         *
         * @param token 클라이언트 JWT Access Token
         */

        log.info("로그아웃 처리 시작: token={}", token);
        String userId;

        if (token == null || token.trim().isEmpty()) {
            log.warn("로그아웃 실패 - 토큰 없음");
            throw new RuntimeException("토큰이 제공되지 않았습니다.");
        }

        try {
            // 1) 토큰 유효성 검사 + userId 추출
            userId = jwtUtil.getUserId(token);
        } catch (Exception e) {
            log.warn("로그아웃 실패 - 유효하지 않은 토큰: token={}", token);
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        try {
            // 2) DB에 저장된 Refresh Token 삭제
            refreshTokenRepository.deleteByUserId(userId);
            log.info("저장된 Refresh Token 삭제 완료: userId={}", userId);

        } catch (Exception e) {
            log.error("Refresh Token 삭제 중 오류 발생: userId={}, error={}", userId, e.toString());
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.");
        }

        log.info("로그아웃 처리 완료: userId={}", userId);
    }
    
    @Transactional
    public void updateUser(String token, UpdateRequest req, String apiKey) {
        /**
         * 회원정보 수정 서비스 로직
         * - token -> userId 추출
         * - 사용자 조회
         * - 전달된 name, pw, apiKey 중 존재하는 값만 업데이트
         */

        log.info("회원정보 수정 처리 시작");

        // 1) JWT에서 userId 추출
        String userId = jwtUtil.getUserId(token);

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // 2) 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 3-1) 이름 변경 (req 기반)
        if (req.getName() != null && !req.getName().isBlank()) {
            user.setName(req.getName());
            log.info("사용자 이름 변경: {}", req.getName());
        }

        // 3-2) 비밀번호 변경 (req 기반)
        if (req.getPw() != null && !req.getPw().isBlank()) {
            String encodedPw = passwordEncoder.encode(req.getPw());
            user.setPw(encodedPw);
            log.info("사용자 비밀번호 변경");
        }

        // 3-3) API Key 변경 
        log.info("사용자 API Key: {}", apiKey);
        if (apiKey != null && !apiKey.isBlank()) {
            user.setApiKey(apiKey);
            log.info("사용자 API Key 변경: {}", apiKey);
        }

        // 4) 저장
        userRepository.save(user);

        log.info("회원정보 수정 완료: userId={}", userId);
    }

    @Transactional
    public void deleteUser(String token, String pw) {
        /**
         * 회원 탈퇴 서비스 로직
         * - token → userId 추출
         * - userId로 사용자 조회
         * - 비밀번호 검증
         * - Refresh Token 삭제
         * - DB에서 사용자 삭제
         *
         * @param token JWT Access Token
         * @param pw    비밀번호 (본인 확인용)
         */
        log.info("회원 탈퇴 처리 시작: token={}", token);
        if (pw == null || pw.isBlank()) {
            log.warn("회원 탈퇴 실패 - 비밀번호 미입력");
            throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
        }
        
        String userId;

        // 1) 토큰 검증 및 userId 추출
        try {
            userId = jwtUtil.getUserId(token);
            log.info("JWT 검증 성공: userId={}", userId);
        } catch (Exception e) {
            log.warn("회원 탈퇴 실패 - 토큰 검증 실패: token={}", token);
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }

        // 2) 유저 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("회원 탈퇴 실패 - 사용자 조회 실패: userId={}", userId);
                return new RuntimeException("사용자 정보를 찾을 수 없습니다.");
            });

        // 3) 비밀번호 검증
        if (!passwordEncoder.matches(pw, user.getPw())) {
            log.warn("회원 탈퇴 실패 - 비밀번호 불일치: userId={}", userId);
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }

        // 4) Refresh Token 삭제 (외래키 문제 예방)
        try {
            refreshTokenRepository.deleteByUserId(userId);
            log.info("Refresh Token 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("회원 탈퇴 실패 - Refresh Token 삭제 중 오류: userId={}, error={}", userId, e.toString());
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }

        // 5) 사용자 삭제
        try {
            userRepository.delete(user);
            log.info("회원 탈퇴 처리 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("회원 탈퇴 실패 - User 삭제 중 오류: userId={}, error={}", userId, e.toString());
            throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    public String validateAccessToken(String token) {
        /**
         * JWT 토큰 유효성 검사
         * - 정상: "VALID"
         * - 그 외:Exception
         */
        log.info("토큰 유효성 검증 시작");
        return jwtUtil.validateToken(token);
    }

    public String reissueAccessToken(String refreshToken) {
        /**
         *  토큰 유효성 검사
         * - 정상: JWT 토큰 반환
         * - 그 외:Exception
         * 
         * @param refreshToken 클라이언트가 보낸 JWT Refresh Token
         * @return 새롭게 발급된 Access Token (String)
         */

        log.info("토큰 재발급 요청 처리 시작: refreshToken={}", refreshToken);

        // 1) Refresh Token 유효성 검사
        jwtUtil.validateRefreshToken(refreshToken); // 내부에서 예외 발생 시 그대로 전파됨
        log.info("Refresh Token 유효성 검증 완료");

        // 2) userId 추출
        String userId = jwtUtil.getUserId(refreshToken);
        log.info("RefreshToken에서 userId 추출 완료: {}", userId);

        if (userId == null || userId.isBlank()) {
            log.warn("Refresh Token에서 userId 추출 실패");
            throw new RuntimeException("사용자 정보를 확인할 수 없습니다. 다시 로그인해주세요.");
        }

        // 3) DB에 저장된 Refresh Token 조회
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("DB에 저장된 Refresh Token 없음: userId={}", userId);
                    return new RuntimeException("저장된 리프레시 토큰이 없습니다. 다시 로그인해야 합니다.");
                });

        // 4) 클라이언트가 보낸 refreshToken과 DB 저장 토큰 비교
        if (!savedToken.getToken().equals(refreshToken)) {
            log.warn("리프레시 토큰 불일치: userId={}", userId);
            throw new RuntimeException("저장된 리프레시 토큰과 일치하지 않습니다.");
        }

        // 5) 만료 여부 확인
        if (System.currentTimeMillis() > savedToken.getExpiry()) {
            log.warn("리프레시 토큰 만료됨: userId={}", userId);
            throw new RuntimeException("리프레시 토큰이 만료되었습니다. 다시 로그인해야 합니다.");
        }

        // 6) 새 Access Token 생성
        String newAccessToken = jwtUtil.createAccessToken(userId);
        log.info("새 Access Token 발급 완료: userId={}", userId);

        return newAccessToken;
    }


    public String getUserApiKey(String token) {
        /**
         * API Key 조회 서비스
         * - 전달된 JWT Access Token에서 사용자 ID를 추출하고,
         *   해당 사용자의 등록된 API Key를 반환하는 로직입니다.
         *
         * 처리 흐름:
         *   1) JWT에서 userId 추출
         *   2) userId가 유효하지 않을 경우 예외 발생
         *   3) DB에서 사용자 정보 조회
         *   4) API Key 존재 여부 확인
         *   5) API Key 반환
         *
         * 예외 처리:
         *   - 잘못된 토큰: RuntimeException → GlobalExceptionHandler에서 401 처리
         *   - 사용자 없음: RuntimeException → GlobalExceptionHandler에서 404 처리 가능
         *   - API Key 없음: IllegalArgumentException → GlobalExceptionHandler에서 400 처리
         */
        log.info("API Key 조회 처리 시작");

        // 1) JWT에서 userId 추출
        String userId = jwtUtil.getUserId(token);
        log.info("JWT 토큰 검증 완료: userId={}", userId);

        if (userId == null || userId.isBlank()) {
            log.warn("API Key 조회 실패 - 잘못된 토큰");
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        // 2) 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("API Key 조회 실패 - 사용자 정보 없음: userId={}", userId);
                return new RuntimeException("정보를 찾을 수 없습니다.");
            });

        // 3) API Key 확인
        if (user.getApiKey() == null || user.getApiKey().isBlank()) {
            log.warn("API Key 조회 실패 - 등록된 API Key 없음: userId={}", userId);
            throw new IllegalArgumentException("등록된 API Key가 없습니다.");
        }

        log.info("API Key 조회 성공: userId={}", userId);

        return user.getApiKey();
    }

    public User getUserInfo(String accessToken) {
        /**
         * 사용자 정보 조회 서비스
         * - 전달된 JWT Access Token에서 사용자 ID를 추출하고,
         *   해당 사용자의 전체 정보를 조회하여 반환합니다.
         *
         * 처리 흐름:
         *   1) JWT에서 userId 추출
         *   2) userId가 유효하지 않을 경우 예외 발생
         *   3) DB에서 사용자 정보 조회
         *   4) 존재하지 않을 경우 예외 발생
         *   5) User 엔티티 반환
         *
         * 예외 처리:
         *   - 잘못된 토큰: ResponseStatusException(401) 발생
         *   - 사용자 없음: ResponseStatusException(404) 발생
         */

        log.info("사용자 정보 조회 처리 시작");

        // 1) JWT에서 userId 추출
        String userId;
        try {
            userId = jwtUtil.getUserId(accessToken);
            log.info("JWT 토큰 검증 완료: userId={}", userId);
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패 - 유효하지 않은 토큰");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        // 2) userId가 비정상적인 경우
        if (userId == null || userId.isBlank()) {
            log.warn("사용자 정보 조회 실패 - JWT에서 추출된 userId가 유효하지 않음");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        // 3) DB에서 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("사용자 정보 조회 실패 - 사용자 정보 없음: userId={}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 사용자 정보를 찾을 수 없습니다.");
                });

        // 4) 조회 성공 로그
        log.info("사용자 정보 조회 성공: id={}, name={}", user.getId(), user.getName());

        // 5) User 엔티티 반환
        return user;
    }

}
