package com.fpoly.java6.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.access-expiration-ms:7200000}")
	private long accessExpirationMs;

	@Value("${app.jwt.refresh-expiration-ms:864000000}")
	private long refreshExpirationMs;

	public String generateAccessToken(UserDetails userDetails) {
		return generateTokenWithType(userDetails, TokenType.ACCESS, accessExpirationMs);
	}

	public String generateRefreshToken(UserDetails userDetails) {
		return generateTokenWithType(userDetails, TokenType.REFRESH, refreshExpirationMs);
	}

	private String generateTokenWithType(UserDetails userDetails, TokenType tokenType, long expirationMs) {
		Map<String, Object> extraClaims = new HashMap<>();
		if (userDetails instanceof CustomUserDetails customUser) {
			extraClaims.put("userId", customUser.getId());
			extraClaims.put("role", customUser.getRoleName());
		}
		extraClaims.put("type", tokenType.name());
		extraClaims.put("jti", UUID.randomUUID().toString());
		return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationMs))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String extractUsernameAllowExpired(String token) {
		return extractClaimAllowExpired(token, Claims::getSubject);
	}

	public TokenType extractTokenType(String token) {
		String type = extractClaim(token, claims -> (String) claims.get("type"));
		return TokenType.valueOf(type);
	}

	public TokenType extractTokenTypeAllowExpired(String token) {
		String type = extractClaimAllowExpired(token, claims -> (String) claims.get("type"));
		return TokenType.valueOf(type);
	}

	public Date extractExpirationAllowExpired(String token) {
		return extractClaimAllowExpired(token, Claims::getExpiration);
	}

	public boolean isAccessTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token)
				&& extractTokenType(token) == TokenType.ACCESS;
	}

	public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token)
				&& extractTokenType(token) == TokenType.REFRESH;
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
	}

	private <T> T extractClaimAllowExpired(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaimsAllowExpired(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaimsAllowExpired(String token) {
		try {
			return extractAllClaims(token);
		} catch (ExpiredJwtException ex) {
			return ex.getClaims();
		}
	}

	private Key getSigningKey() {
		SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		return key;
	}
}
