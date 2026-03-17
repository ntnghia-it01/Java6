package com.fpoly.java6.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {
	private String token;
	private String tokenType = "Bearer";
	private String refreshToken;
	private UserResponse user;

	public AuthResponse(String token, String refreshToken, UserResponse user) {
		this.token = token;
		this.refreshToken = refreshToken;
		this.user = user;
	}
}
