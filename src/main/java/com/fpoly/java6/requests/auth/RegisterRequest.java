package com.fpoly.java6.requests.auth;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterRequest {
	@NotBlank
	private String username;

	@NotBlank
	@Length(min = 6)
	private String password;

	@NotBlank
	private String name;

	@NotBlank
	@Email
	private String email;
}
