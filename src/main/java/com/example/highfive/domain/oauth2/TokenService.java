package com.example.highfive.domain.oauth2;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class TokenService{
    private String secretKey = "asdfkjhasdflkjhasdfjklahdslka";

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }



    public String generateToken(String email) {

        long nowMillis = System.currentTimeMillis();
        //3시간
        long tokenPeriod = 1000L * 3L * 60L * 60L;
        Date now = new Date(nowMillis);

        java.util.Map<String, Object>payloads = new HashMap<>();
        payloads.put("email", email);
        payloads.put("iat", now.getTime());
        payloads.put("exp", now.getTime()+ tokenPeriod);

        JwtBuilder builder = Jwts.builder()
                .setClaims(payloads)
                .signWith(getSignKey());
        return builder.compact();
    }


    public boolean verifyToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return claims.getBody()
                    .getExpiration()
                    .after(new Date());
        } catch (Exception e) {
            return false;
        }
    }


    private Key getSignKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    public String getUid(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }
}
