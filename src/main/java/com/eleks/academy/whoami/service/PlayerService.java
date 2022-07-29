package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.database.entity.Player;
import com.eleks.academy.whoami.dto.AuthenticationDTO;
import com.eleks.academy.whoami.dto.CreatePlayerDto;
import com.eleks.academy.whoami.dto.ResponsePlayerDto;

import java.util.Optional;

public interface PlayerService {

	ResponsePlayerDto createPlayer(CreatePlayerDto createPlayerDto);

	Optional<Player> findByEmail(String email);

	ResponsePlayerDto loginPlayer(AuthenticationDTO player);
}
