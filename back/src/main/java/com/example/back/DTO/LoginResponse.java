package com.example.back.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String status;   // success or error
    private String message;  
    private String userId;   
}
