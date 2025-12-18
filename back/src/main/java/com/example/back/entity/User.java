package com.example.back.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @Column(nullable = false, length = 100)      
    private String id;                            // PK

    @Column(nullable = false, length = 255)
    private String pw;                            // 비밀번호
    
    @Column(nullable = false, length = 100)
    private String name;                          // 이름
    
    @Column(name = "api_key", length = 255)
    private String apiKey;
}
