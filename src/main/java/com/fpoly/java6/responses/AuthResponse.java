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
	private UserResponse user;

	public AuthResponse(String token, UserResponse user) {
		this.token = token;
		this.user = user;
	}
}
