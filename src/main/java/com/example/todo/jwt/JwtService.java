package com.example.todo.jwt;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtService {
  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.expiration-seconds}")
  private Integer expirationSeconds;

  public String generateToken(String username) {
    return Jwts.builder()
    .setClaims(new HashMap<>())
    .setIssuedAt(Date.from(Instant.now()))
    .setExpiration(Date.from(Instant.now().plusSeconds(expirationSeconds)))
    .setSubject(username)
    .signWith(getKey(), SignatureAlgorithm.HS256)
    .compact();
  }

  public String getUsername(String token) {
    return getClaims(token).getSubject();
  }

  public boolean isValid(UserDetails user, String token) {
    try {
      return user.getUsername().equals(getUsername(token)) 
      && !getClaims(token).getExpiration().before(new Date());
    } catch (JwtException e) {
      throw new JwtException("Invalid JWT token.");
    }
  }

  private Key getKey() {
    byte[] decodeKey = Decoders.BASE64.decode(this.secretKey);
    return Keys.hmacShaKeyFor(decodeKey);
  }

  private Claims getClaims(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(getKey())
      .build()
      .parseClaimsJws(token)
      .getBody();
  }
}
