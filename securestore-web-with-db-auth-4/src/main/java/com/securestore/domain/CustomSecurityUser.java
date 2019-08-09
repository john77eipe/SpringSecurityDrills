package com.securestore.domain;

import org.springframework.security.core.userdetails.UserDetails;

public class CustomSecurityUser extends UserAccount implements UserDetails {
	
	private static final long serialVersionUID = -4381938875186527688L;

	public CustomSecurityUser() {
	}

	public CustomSecurityUser(UserAccount user) {
		this.setAuthorities(user.getAuthorities());
		this.setId(user.getId());
		this.setPassword(user.getPassword());
		this.setUsername(user.getUsername());
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
		return true;
	}
}
