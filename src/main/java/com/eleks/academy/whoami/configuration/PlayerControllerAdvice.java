package com.eleks.academy.whoami.configuration;

import com.eleks.academy.whoami.core.exception.ErrorResponse;
import com.eleks.academy.whoami.core.exception.PlayerCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
public class PlayerControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(PlayerCreationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> handleCreatePlayerException(PlayerCreationException e) {
		return ResponseEntity.badRequest()
				.body(new ErrorResponse("Registration player failed !", List.of(e.getMessage())));
	}

	@ExceptionHandler(PlayerCreationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Object> authorithationPlayerException(PlayerCreationException e) {
		return ResponseEntity.badRequest()
				.body(new ErrorResponse("Registration player failed !", List.of(e.getMessage())));
	}
}
