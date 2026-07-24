package com.example.studyroom.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
	private final SecretKey secretKey;
	private final long expirationSeconds;

	public JwtTokenProvider(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-seconds}") long expirationSeconds
	) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationSeconds = expirationSeconds;
	}

	public String createToken(String email) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(expirationSeconds)))
				.signWith(secretKey)
				.compact();
	}

	public String getEmail(String token) {
		return Jwts.parser().verifyWith(secretKey).build()
				.parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean isValid(String token) {
		try {
			getEmail(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}
}
