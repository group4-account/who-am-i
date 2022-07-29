package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.exception.PlayerCreationException;
import com.eleks.academy.whoami.database.repository.PlayerRepository;
import com.eleks.academy.whoami.dto.CreatePlayerDto;
import com.eleks.academy.whoami.mapper.CreatePlayerMapper;
import com.eleks.academy.whoami.mapper.PlayerReadMapper;
import com.eleks.academy.whoami.service.impl.PlayerServiceImpl;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PlayerServiceTest {
	private PlayerRepository playerRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private CreatePlayerMapper createPlayerMapper;
	private PlayerService playerService;
	@Autowired
	private PlayerReadMapper playerReadMapper;

	@BeforeEach
	void init() {
		playerRepository = Mockito.mock(PlayerRepository.class);
		createPlayerMapper = new CreatePlayerMapper(passwordEncoder);
	//	playerService = new PlayerServiceImpl(playerRepository, createPlayerMapper, playerReadMapper,);
	}

	@Test
	void createFailBecauseEmailAlreadyExist() {
		when(playerRepository.findByEmail(any())).thenThrow(PlayerCreationException.class);
		assertThrows(PlayerCreationException.class, () -> this.playerService.createPlayer(CreatePlayerDto.builder()
				.username("Test")
				.email("test@gmail.com")
				.password("AA45aa$aad")
				.build()));
	}
}
