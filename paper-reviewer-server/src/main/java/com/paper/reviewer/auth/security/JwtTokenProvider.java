package com.paper.reviewer.auth.security;

import com.paper.reviewer.config.JwtProperties;
import com.paper.reviewer.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey signingKey;
    private final Duration expiration;
    private final Clock clock;

    public JwtTokenProvider(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(properties.expirationMinutes());
        this.clock = Clock.systemUTC();
    }

    public String createToken(User user) {
        Date issuedAt = Date.from(clock.instant());
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("email", user.email())
                .issuedAt(issuedAt)
                .expiration(Date.from(clock.instant().plus(expiration)))
                .signWith(signingKey)
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build()
                .parseSignedClaims(token).getPayload();
        return new AuthenticatedUser(Long.valueOf(claims.getSubject()), claims.get("email", String.class));
    }
}
