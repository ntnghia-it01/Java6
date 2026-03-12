package com.fpoly.java6.responses;

import com.fpoly.java6.entities.User;
import com.fpoly.java6.security.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponse {
	private int id;
	private String username;
	private String name;
	private String email;
	private boolean status;
	private int role;
	private String roleName;

	public static UserResponse from(User user) {
		UserRole userRole = UserRole.fromValue(user.getRole());
		return new UserResponse(user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.isStatus(),
				user.getRole(), userRole.name());
	}
}
