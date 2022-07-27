package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.exception.PlayerCreationException;
import com.eleks.academy.whoami.database.repository.PlayerRepository;
import com.eleks.academy.whoami.dto.PlayersDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomPlayerDetailsService implements UserDetailsService {
	private final PlayerRepository playerRepository;

	public CustomPlayerDetailsService(PlayerRepository playerRepository) {
		this.playerRepository = playerRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return this.playerRepository.findByEmail(email)
				.map(PlayersDetails::new)
				.orElseThrow(() -> new PlayerCreationException("Player not found!"));
	}

}
