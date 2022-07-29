package com.eleks.academy.whoami.mapper;

import com.eleks.academy.whoami.database.entity.Player;
import com.eleks.academy.whoami.dto.ResponsePlayerDto;
import com.eleks.academy.whoami.security.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlayerReadMapper implements Mapper<Player, ResponsePlayerDto> {
private final JWTUtils jwtUtils;
	@Autowired
	public PlayerReadMapper(JWTUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	@Override
	public ResponsePlayerDto map(Player object) {
		return new ResponsePlayerDto(
				object.getUsername(),
				jwtUtils.generateToken(object.getEmail()));
	}
}
