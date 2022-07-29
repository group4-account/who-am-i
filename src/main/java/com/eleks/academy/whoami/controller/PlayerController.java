package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.dto.AuthenticationDTO;
import com.eleks.academy.whoami.dto.CreatePlayerDto;
import com.eleks.academy.whoami.dto.ResponsePlayerDto;
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
@RequestMapping("/users")
@RequiredArgsConstructor
public class PlayerController {

	private final PlayerService playerService;

	@PostMapping("/registration")
	public ResponseEntity<ResponsePlayerDto> create(@Valid @RequestBody CreatePlayerDto player) {
		return status(HttpStatus.CREATED).body(this.playerService.createPlayer(player));
	}

	@PostMapping("/authorisation")
	public ResponseEntity<ResponsePlayerDto> create(@Valid @RequestBody AuthenticationDTO player) {
		return status(HttpStatus.ACCEPTED).body(this.playerService.loginPlayer(player));
	}
}
