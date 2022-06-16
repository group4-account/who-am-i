package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import com.eleks.academy.whoami.repository.GameRepository;
import com.eleks.academy.whoami.repository.impl.GameInMemoryRepository;
import com.eleks.academy.whoami.service.impl.GameServiceImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


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
        String player = "Anton1";
        gameService.enrollToGame(gameId, player);
        assertNotNull(gameService.findByIdAndPlayer(gameId, player));
        Optional<GameDetails> byIdAndPlayer = gameService.findByIdAndPlayer(gameId, player);
        assertThat(byIdAndPlayer).isNotEmpty();
        Optional<SynchronousPlayer> synchronousPlayer =
                byIdAndPlayer.map(GameDetails::getPlayers).map(a -> a.get(1)).map(PlayerWithState::getPlayer);
        assertEquals(synchronousPlayer.get().getName(), player);
        assertNotNull(byIdAndPlayer.map(GameDetails::getId));
        assertNotNull(byIdAndPlayer.map(GameDetails::getStatus));
        assertNotNull(byIdAndPlayer.map(GameDetails::getPlayers));
        assertEquals(byIdAndPlayer.get().getPlayers().size(), 2);
    }

    @Test
    @SneakyThrows
    void testSetStateInSuggestCharacters() {
        final String player = "Anton";
        final PlayerState previousState = PlayerState.NOT_READY;
        final PlayerState updateState = PlayerState.READY;
        CharacterSuggestion character = new CharacterSuggestion("char");
        gameService.enrollToGame(gameId, player);
        gameService.enrollToGame(gameId, player+"1");
        PlayerState playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().get(0).getState();
        assertEquals(playerState, previousState);
        gameService.suggestCharacter(gameId, player, character);
        playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().get(2).getState();
        assertEquals(updateState, playerState);

    }
    @Test
    @SneakyThrows
    void testAssignCharacter() {
        final String player = "Anton";
        final PlayerState previousState = PlayerState.NOT_READY;
        final PlayerState updateState = PlayerState.READY;
        CharacterSuggestion character = new CharacterSuggestion("char");
        CharacterSuggestion character1 = new CharacterSuggestion("char1");
        CharacterSuggestion character2 = new CharacterSuggestion("char2");
        gameService.enrollToGame(gameId, player);
        gameService.enrollToGame(gameId, player+"1");
        PlayerState playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().get(0).getState();
        assertEquals(playerState, previousState);
        gameService.suggestCharacter(gameId, player, character);
        gameService.suggestCharacter(gameId, "host", character1);
        gameService.suggestCharacter(gameId, "Anton1", character2);
        playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().get(2).getState();
        System.out.println(gameService.startGame(gameId, player).get()
                .getPlayers().stream().map(PlayerWithState::getPlayer).collect(Collectors.toList()));
        System.out.println();
        Map<String, PlayerWithState> playerWithStateMap = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().stream()
                .collect(Collectors.toMap(a-> a.getPlayer().getName(), Function.identity()));
        assertEquals(updateState, playerState);
        assertNotEquals(character,playerWithStateMap.get(player).getPlayer().getCharacter());
        assertNotEquals(character1,playerWithStateMap.get("host").getPlayer().getCharacter());
        assertNotEquals(character2,playerWithStateMap.get("Anton1").getPlayer().getCharacter());

    }

    @Test
    void createGame() {
        String waitingForPlayersStatus = "com.eleks.academy.whoami.core.state.WaitingForPlayers";
        GameDetails gameDetails = gameService.createGame("player-1", gameRequest);
        String status = gameDetails.getStatus();
        assertEquals(waitingForPlayersStatus, status);
    }
}
