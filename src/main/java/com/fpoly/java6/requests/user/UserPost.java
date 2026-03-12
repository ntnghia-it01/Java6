package com.fpoly.java6.requests.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserPost extends UserPut {
	@NotBlank
	private String username;
	@NotBlank
	@Email
	private String email;
}
