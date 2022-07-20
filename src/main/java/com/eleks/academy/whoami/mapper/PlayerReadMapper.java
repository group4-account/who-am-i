package com.eleks.academy.whoami.mapper;

import com.eleks.academy.whoami.database.entity.Player;
import com.eleks.academy.whoami.dto.PlayerDto;
import org.springframework.stereotype.Component;

@Component
public class PlayerReadMapper implements Mapper<Player, PlayerDto> {

	@Override
	public PlayerDto map(Player object) {
		return new PlayerDto(
				object.getId(),
				object.getEmail());
	}
}
