package com.example.back.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // 독립된 PK

    @OneToOne
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,   // 하나의 user마다 하나의 refreshToken
        foreignKey = @ForeignKey(name = "fk_refresh_user")
    )
    private User user;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private Long expiry;

    public RefreshToken() {}

    public RefreshToken(User user, String token, Long expiry) {
        this.user = user;
        this.token = token;
        this.expiry = expiry;
    }

    public void updateToken(String token, Long expiry) {
        this.token = token;
        this.expiry = expiry;
    }
}
