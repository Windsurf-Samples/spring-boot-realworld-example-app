package io.spring.articles.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.spring.articles.core.service.JwtService;
import io.spring.articles.core.user.User;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultJwtService implements JwtService {
  private SecretKey key;
  private int sessionTime;

  public DefaultJwtService(
      @Value("${jwt.secret:secret}") String secret,
      @Value("${jwt.sessionTime:86400}") int sessionTime) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.sessionTime = sessionTime;
  }

  @Override
  public String toToken(User user) {
    return Jwts.builder()
        .setSubject(user.getId())
        .setExpiration(new Date(System.currentTimeMillis() + sessionTime * 1000))
        .signWith(key, SignatureAlgorithm.HS512)
        .compact();
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return Optional.ofNullable(claimsJws.getBody().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
