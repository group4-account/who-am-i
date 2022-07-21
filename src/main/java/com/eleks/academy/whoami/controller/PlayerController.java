package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.dto.CreatePlayerDto;
import com.eleks.academy.whoami.dto.PlayerDto;
import com.eleks.academy.whoami.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

	private final PlayerService playerService;

	@PostMapping
	public ResponseEntity<PlayerDto> create(@Valid @RequestBody CreatePlayerDto player) {
		return status(HttpStatus.CREATED).body(this.playerService.createPlayer(player));
	}
}
