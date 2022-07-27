package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.service.PlayerService;
import com.eleks.academy.whoami.service.impl.PlayerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class PlayerControllerTest {
	private final PlayerService playerService = mock(PlayerServiceImpl.class);
	private final PlayerController playerController = new PlayerController(playerService);
	private MockMvc mockMvc;

	@BeforeEach
	public void setMockMvc() {
		mockMvc = MockMvcBuilders.standaloneSetup(playerController).build();
	}

	@Test
	void create() throws Exception {
		mockMvc.perform(post("/players")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								    "username": "Test",
								    "email": "test@gmail.com",
								    "password": "AA45aa$aad"
								}"""))
				.andExpect(
						status().isCreated()
				);
	}

	@Test
	void failCreate() throws Exception {
		mockMvc.perform(post("/players")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								    "username": "Test_",
								    "email": "test@gmail.com",
								    "password": "AA45aa$aad"
								}"""))
				.andExpect(
						status().isBadRequest()
				);
	}
}
