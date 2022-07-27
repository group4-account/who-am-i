package com.eleks.academy.whoami.security;

import com.eleks.academy.whoami.service.impl.CustomPlayerDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Objects;

@RequiredArgsConstructor
public class AuthProvider implements AuthenticationProvider {
	@Autowired
	private final CustomPlayerDetailsService playerDetailsService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String email = authentication.getName();
		UserDetails playerDetails = playerDetailsService.loadUserByUsername(email);
		String encodedPassword = passwordEncoder.encode(authentication.getCredentials().toString());
		if (!Objects.equals(encodedPassword,
				playerDetails.getPassword())) {
			throw new BadCredentialsException("Incorrect password");
		}
		return new UsernamePasswordAuthenticationToken(playerDetails, encodedPassword,
				Collections.emptyList());

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}
}
