package com.eleks.academy.whoami.mapper;

import com.eleks.academy.whoami.database.entity.Player;
import com.eleks.academy.whoami.dto.PlayerDto;
import com.eleks.academy.whoami.security.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlayerReadMapper implements Mapper<Player, PlayerDto> {
private final JWTUtils jwtUtils;
	@Autowired
	public PlayerReadMapper(JWTUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	@Override
	public PlayerDto map(Player object) {
		return new PlayerDto(
				object.getId(),
				jwtUtils.generateToken(object.getEmail()));
	}
}
