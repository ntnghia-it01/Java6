package com.fpoly.java6.security;

public enum UserRole {
	USER(0, "ROLE_USER"),
	ADMIN(1, "ROLE_ADMIN");

	private final int value;
	private final String authority;

	UserRole(int value, String authority) {
		this.value = value;
		this.authority = authority;
	}

	public int getValue() {
		return value;
	}

	public String getAuthority() {
		return authority;
	}

	public static UserRole fromValue(int value) {
		for (UserRole role : values()) {
			if (role.value == value) {
				return role;
			}
		}
		return USER;
	}
}
