package com.example.back.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="book_id")
    private Long id; // PK (book id)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 작성자 id

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category categoryId; // 카테고리

    @Column(nullable = false, length = 100)
    private String title; // 작품 제목

    @Column(nullable = false, length = 100)
    private String description; // 작품 설명

    @Column(nullable = false, length = 1000)
    private String content; // 작품 내용

    @Column(name = "image_url", length = 500)
    private String imageUrl;   // AI 생성 표지 이미지 URL

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at;
    
    @Column(name = "updated_at")
    private LocalDateTime updated_at;
}
