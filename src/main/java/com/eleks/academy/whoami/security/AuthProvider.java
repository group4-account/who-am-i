package com.eleks.academy.whoami.security;

import com.eleks.academy.whoami.dto.AuthenticationDTO;
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
import org.springframework.stereotype.Component;

import java.util.Collections;

@RequiredArgsConstructor
@Component
public class AuthProvider implements AuthenticationProvider {
	@Autowired
	private final CustomPlayerDetailsService playerDetailsService;
	private final PasswordEncoder passwordEncoder;

	public Authentication authenticate(AuthenticationDTO authentication) throws AuthenticationException {
		String email = authentication.getEmail();
		UserDetails playerDetails = playerDetailsService.loadUserByUsername(email);
		String password = playerDetailsService.loadUserByUsername(email).getPassword();
		String encodedPassword = authentication.getPassword();
		if (!passwordEncoder.matches(encodedPassword, password)) {
			throw new BadCredentialsException("Incorrect password");
		}
		return new UsernamePasswordAuthenticationToken(playerDetails, encodedPassword,
				Collections.emptyList());

	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}
}
