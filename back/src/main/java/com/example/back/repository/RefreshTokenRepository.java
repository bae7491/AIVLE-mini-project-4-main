package com.example.back.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.back.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(String userId);  // PK가 아니라 user_id 컬럼 기반 조회
    void deleteByUserId(String userId);
}

