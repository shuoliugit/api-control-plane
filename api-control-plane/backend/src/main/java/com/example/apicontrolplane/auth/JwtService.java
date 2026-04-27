package com.example.apicontrolplane.auth;

import com.example.apicontrolplane.user.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final String issuer;
  private final long ttlMinutes;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.issuer = issuer;
    this.ttlMinutes = ttlMinutes;
  }

  public String issue(UserAccount user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(issuer)
        .subject(user.getEmail())
        .claim("uid", user.getId().toString())
        .claim("role", user.getRole().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttlMinutes * 60)))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).requireIssuer(issuer).build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
