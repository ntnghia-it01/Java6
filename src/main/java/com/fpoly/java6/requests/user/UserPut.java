package com.fpoly.java6.requests.user;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserPut {
	@NotBlank
	@Length(min = 6)
	private String password;
	@NotBlank
	private String name;
}
