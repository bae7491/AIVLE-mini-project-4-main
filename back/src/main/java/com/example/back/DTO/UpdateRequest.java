package com.example.back.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequest {
    private String name;
    private String pw;
    private String apikey;
}
