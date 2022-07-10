package com.example.highfive.domain.oauth2;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
public class Token {
    private String token;

    public Token(String token) {
        this.token = token;
    }
}
