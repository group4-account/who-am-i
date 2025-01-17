package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.exception.PlayerCreationException;
import com.eleks.academy.whoami.database.entity.Player;
import com.eleks.academy.whoami.database.repository.PlayerRepository;
import com.eleks.academy.whoami.dto.CreatePlayerDto;
import com.eleks.academy.whoami.dto.PlayerDto;
import com.eleks.academy.whoami.mapper.CreatePlayerMapper;
import com.eleks.academy.whoami.mapper.PlayerReadMapper;
import com.eleks.academy.whoami.service.PlayerService;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@NoArgsConstructor
public class PlayerServiceImpl implements PlayerService {

	private PlayerRepository playerRepository;
	private CreatePlayerMapper createPlayerMapper;
	private PlayerReadMapper playerReadMapper;

	@Autowired
	public PlayerServiceImpl(PlayerRepository playerRepository, CreatePlayerMapper createPlayerMapper,
							 PlayerReadMapper playerReadMapper) {
		this.playerRepository = playerRepository;
		this.createPlayerMapper = createPlayerMapper;
		this.playerReadMapper = playerReadMapper;
	}

	@Override
	@Transactional
	public PlayerDto createPlayer(CreatePlayerDto createPlayerDto) {
		this.findByEmail(createPlayerDto.getEmail())
				.ifPresent(then -> {
					throw new PlayerCreationException("Player with this email already exist!");
				});
		return Optional.of(createPlayerDto)
				.map(dto -> this.createPlayerMapper.map(dto))
				.map(entity -> this.playerRepository.save(entity))
				.map(object -> this.playerReadMapper.map(object))
				.orElseThrow();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Player> findByEmail(String email) {
		return this.playerRepository.findByEmail(email);
	}
}
