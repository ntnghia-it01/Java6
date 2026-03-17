package com.fpoly.java6.entities;

import java.util.Date;

import com.fpoly.java6.security.TokenType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "token", nullable = false, length = 1000, unique = true)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(name = "token_type", nullable = false, length = 20)
	private TokenType tokenType;

	@Column(name = "revoked_at", nullable = false)
	private Date revokedAt;

	@Column(name = "expires_at", nullable = false)
	private Date expiresAt;
}
