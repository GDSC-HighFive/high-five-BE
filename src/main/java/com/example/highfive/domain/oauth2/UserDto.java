package com.example.highfive.domain.oauth2;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserDto {
    private String email;
    private String name;
    private String picture;
    private String googleAccessToken;

    @Builder
    public UserDto(String email, String name, String picture, String googleAccessToken) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.googleAccessToken = googleAccessToken;
    }
}