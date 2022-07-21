package com.eleks.academy.whoami.mapper;

import com.eleks.academy.whoami.database.entity.Player;
import com.eleks.academy.whoami.dto.CreatePlayerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class CreatePlayerMapper implements Mapper<CreatePlayerDto, Player> {
	@Autowired
	private final PasswordEncoder passwordEncoder;

	@Override
	public Player map(CreatePlayerDto dto) {
		Player player = new Player();
		copy(dto, player);

		return player;
	}

	private void copy(CreatePlayerDto dto, Player player) {
		player.setUsername(dto.getUsername());
		player.setEmail(dto.getEmail());

		ofNullable(dto.getPassword())
				.filter(StringUtils::hasText)
				.map(passwordEncoder::encode)
				.ifPresent(player::setPassword);

	}
}
