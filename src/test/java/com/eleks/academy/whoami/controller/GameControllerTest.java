package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.configuration.GameControllerAdvice;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.service.impl.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

	private final GameServiceImpl gameService = mock(GameServiceImpl.class);
	private final GameController gameController = new GameController(gameService);
	private final NewGameRequest gameRequest = new NewGameRequest();
	private MockMvc mockMvc;

	@BeforeEach
	public void setMockMvc() {
		mockMvc = MockMvcBuilders.standaloneSetup(gameController)
				.setControllerAdvice(new GameControllerAdvice()).build();
		gameRequest.setMaxPlayers(5);
	}

	@Test
	void findAvailableGames() throws Exception {
		this.mockMvc.perform(
						MockMvcRequestBuilders.get("/games")
								.header("X-Player", "player"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotHaveJsonPath());
	}
//g
	@Test
	void createGame() throws Exception {
		GameDetails gameDetails = new GameDetails();
		gameDetails.setId("12613126");
		gameDetails.setStatus("WaitingForPlayers");
		when(gameService.createGame(eq("player"), any(NewGameRequest.class))).thenReturn(gameDetails);
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games")
								.header("X-Player", "player")
								.contentType(MediaType.APPLICATION_JSON)
								.content("{\n" +
										"    \"maxPlayers\": 2\n" +
										"}"))
				.andExpect(status().isCreated());
	}

	@Test
	void suggestCharacter() throws Exception {
		doNothing().when(gameService).suggestCharacter(eq("1234"), eq("player"), any(CharacterSuggestion.class));
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games/1234/characters")
								.header("X-Player", "player")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "character":"robot?",
										    "name" : "name"
										}"""))
				.andExpect(status().isOk());
		verify(gameService, times(1)).suggestCharacter(eq("1234"), eq("player"), any(CharacterSuggestion.class));
	}
	@Test
	void failValidationSuggestCharacter() throws Exception {
		doNothing().when(gameService).suggestCharacter(eq("1234"), eq("player"),
				eq(new CharacterSuggestion("Batman", null)));
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games/1234/characters")
								.header("X-Player", "player")
								.contentType(APPLICATION_JSON)
								.content("""
                                        {
                                            "character": "a",
                                            "name": "name"
                                        }"""))
				.andExpect(status().isBadRequest());
	}
	@Test
	void failValidationName() throws Exception {
		doNothing().when(gameService).suggestCharacter(eq("1234"), eq("player"),
				eq(new CharacterSuggestion("Batman", null)));
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games/1234/characters")
								.header("X-Player", "p")
								.contentType(APPLICATION_JSON)
								.content("""
                                        {
                                            "character": "Batman"
                                        }"""))
				.andExpect(status().isBadRequest());
	}
}
