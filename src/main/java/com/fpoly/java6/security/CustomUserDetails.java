package com.fpoly.java6.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fpoly.java6.entities.User;

public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;

	private final User user;

	public CustomUserDetails(User user) {
		this.user = user;
	}

	public int getId() {
		return user.getId();
	}

	public int getRoleValue() {
		return user.getRole();
	}

	public String getRoleName() {
		return UserRole.fromValue(user.getRole()).name();
	}

	public User getUser() {
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		String authority = UserRole.fromValue(user.getRole()).getAuthority();
		return List.of(new SimpleGrantedAuthority(authority));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.isStatus();
	}
}
