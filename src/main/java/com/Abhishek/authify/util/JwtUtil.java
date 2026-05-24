package com.Abhishek.authify.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwtSecretKey}")

    private String SECRET_KEY;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String,Object> cliams=new HashMap<>();
       return createToken(cliams,userDetails.getUsername());
    }

    private String createToken(Map<String, Object> cliams, String email) {
       return Jwts.builder()
                .setClaims(cliams)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*60*10))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims=extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public String extractEmail(String token) {
      return   extractClaim(token,Claims::getSubject);
    }

    public Date extractExpiration(String token) {
       return extractClaim(token,Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token)
    {
       return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token,UserDetails userDetails) {
       final String email= extractEmail(token);
       return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


}
