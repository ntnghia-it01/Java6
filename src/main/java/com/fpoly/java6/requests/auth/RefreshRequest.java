package com.fpoly.java6.requests.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshRequest {
	@NotBlank
	private String refreshToken;
	@NotBlank
	private String accessToken;
}
