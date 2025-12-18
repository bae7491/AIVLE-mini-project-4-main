package com.example.back.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String id;
    private String pw;
    private String name;
    private String apikey;
}
