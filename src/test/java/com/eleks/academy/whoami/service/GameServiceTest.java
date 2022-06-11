package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.repository.GameRepository;
import com.eleks.academy.whoami.repository.impl.GameInMemoryRepository;
import com.eleks.academy.whoami.service.impl.GameServiceImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class GameServiceTest {
    private final GameRepository gameRepository = new GameInMemoryRepository();
    private final GameServiceImpl gameService = new GameServiceImpl(gameRepository);
    private final NewGameRequest gameRequest = new NewGameRequest();
    private String gameId;

    @BeforeEach
    public void setMaxPlayers() {
        gameRequest.setMaxPlayers(3);
        gameId = gameService.createGame("host", gameRequest).getId();
    }

    @Test
    void findAvailableGames() {
        List<GameLight> games = gameService.findAvailableGames("player");
        assertEquals(1, games.size());
    }

    @Test
    public void enrollToGame() {
        String player = "Anton";
        gameService.enrollToGame(gameId, player);
        assertNotNull(gameService.findByIdAndPlayer(gameId, player));
        Optional<GameDetails> byIdAndPlayer = gameService.findByIdAndPlayer(gameId, player);
        assertThat(byIdAndPlayer).isNotEmpty();
        assertNotNull(byIdAndPlayer.map(GameDetails::getId));
        assertNotNull(byIdAndPlayer.map(GameDetails::getStatus));
        assertNotNull(byIdAndPlayer.map(GameDetails::getPlayers));
    }

    @Test
    @SneakyThrows
    void suggestCharacterAndSetPlayerNames() {
        final String player = "Anton";
        final String previousName = "Player-1";
        CharacterSuggestion character = new CharacterSuggestion("char");
        gameService.enrollToGame(gameId, player);
        Optional<SynchronousPlayer> synchronousPlayer = this.gameRepository.findById(gameId)
                .flatMap(game -> game.findPlayer(player));
        String playerName = synchronousPlayer.get().getName();
        assertEquals(previousName, playerName);
        gameService.suggestCharacter(gameId, player, character);
        playerName = synchronousPlayer.get().getName();
        assertEquals(player, playerName);

    }

    @Test
    void createGame() {
        String waitingForPlayersStatus = "com.eleks.academy.whoami.core.state.WaitingForPlayers";
        GameDetails gameDetails = gameService.createGame("player-1", gameRequest);
        String status = gameDetails.getStatus();
        assertEquals(waitingForPlayersStatus, status);
    }
}