package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.database.entity.Player;
import com.eleks.academy.whoami.dto.CreatePlayerDto;
import com.eleks.academy.whoami.dto.PlayerDto;

import java.util.Optional;

public interface PlayerService {

	PlayerDto createPlayer(CreatePlayerDto createPlayerDto);

	Optional<Player> findByEmail(String email);
}
