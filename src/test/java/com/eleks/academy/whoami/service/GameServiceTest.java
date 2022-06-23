package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousGame;
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
import java.util.concurrent.atomic.AtomicReference;
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
        final String player = "Player-1";
        final String player1 = "Player-2";
        final String newPlayer = "Anton";
        final String newPlayer1 = "Anton-1";
        final PlayerState previousState = PlayerState.NOT_READY;
        final PlayerState updateState = PlayerState.READY;
        CharacterSuggestion character = new CharacterSuggestion("char");
        gameService.enrollToGame(gameId, player);
        gameService.enrollToGame(gameId, player1);
        PlayerState playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().get(0).getState();
        assertEquals(playerState, previousState);
        gameService.suggestCharacter(gameId, player, character);
        playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(newPlayer).isPresent())
                .map(GameDetails::of).get().getPlayers().get(1).getState();
        assertEquals(updateState, playerState);

    }
    @Test
    @SneakyThrows
    void testAssignCharacter() {
        final String player = "Player-1";
        final String player1 = "Player-2";
        final String player2 = "host";
        final String char1 = "char";
        final String char2 = "char1";
        final String char3 = "char2";
        final PlayerState previousState = PlayerState.NOT_READY;
        final PlayerState updateState = PlayerState.READY;
        CharacterSuggestion character = new CharacterSuggestion(char1);
        CharacterSuggestion character1 = new CharacterSuggestion(char2);
        CharacterSuggestion character2 = new CharacterSuggestion(char3);
        gameService.enrollToGame(gameId, player);
        gameService.enrollToGame(gameId, player1);
        PlayerState playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().get(0).getState();
        assertEquals(playerState, previousState);
        gameService.suggestCharacter(gameId, player, character);
        gameService.suggestCharacter(gameId, player1, character1);
        gameService.suggestCharacter(gameId, player2, character2);
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
        assertNotEquals(char1,playerWithStateMap.get(player).getPlayer().getCharacter());
        assertNotEquals(char2,playerWithStateMap.get(player1).getPlayer().getCharacter());
        assertNotEquals(char3,playerWithStateMap.get(player2).getPlayer().getCharacter());

        System.out.println(this.gameRepository.findById(gameId).get().getStatus());

    }

    @Test
    void createGame() {
        String waitingForPlayersStatus = "com.eleks.academy.whoami.core.state.WaitingForPlayers";
        GameDetails gameDetails = gameService.createGame("player-1", gameRequest);
        String status = gameDetails.getStatus();
        assertEquals(waitingForPlayersStatus, status);
    }
    @Test
    void leaveTheGame() {
        final String playerId = "player";
        final String playerId2 = "player2";
        AtomicReference<Integer> id1 = new AtomicReference<>();
        Optional<SynchronousGame> game = gameRepository.findById(gameId);
        game.ifPresent(a -> id1.set(a.getPlayersInGame().size()));
        gameService.enrollToGame(gameId, playerId);
        game.ifPresent(a -> id1.set(a.getPlayersInGame().size()));
        assertThat(id1.get()).isEqualTo(2);
        gameService.leaveGame(gameId, playerId);
        game.ifPresent(a -> id1.set(a.getPlayersInGame().size()));
        assertThat(id1.get()).isEqualTo(1);
        gameService.enrollToGame(gameId, playerId2);
        gameService.enrollToGame(gameId, "sas");
        game.ifPresent(a -> id1.set(a.getPlayersInGame().size()));
        assertThat(id1.get()).isEqualTo(3);


    }
}
