package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
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
        gameRequest.setMaxPlayers(4);
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
        assertEquals(synchronousPlayer.get().getId(), player);
        assertNotNull(byIdAndPlayer.map(GameDetails::getId));
        assertNotNull(byIdAndPlayer.map(GameDetails::getStatus));
        assertNotNull(byIdAndPlayer.map(GameDetails::getPlayers));
        assertEquals(byIdAndPlayer.get().getPlayers().size(), 2);
    }

    @Test
    @SneakyThrows
    void setStateInSuggestCharacters() {
        final String player = "Player-1";
        final String player1 = "Player-2";
        final PlayerState previousState = PlayerState.NOT_READY;
        final PlayerState updateState = PlayerState.READY;
        CharacterSuggestion character = new CharacterSuggestion("char", null);
        gameService.enrollToGame(gameId, player);
        gameService.enrollToGame(gameId, player1);
        PlayerState playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().get(0).getState();
        assertEquals(playerState, previousState);
        gameService.suggestCharacter(gameId, player, character);
        playerState = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().get(1).getState();
        assertEquals(updateState, playerState);

    }
    @Test
    @SneakyThrows
    void assignCharacter() {
        final String player = "Player-1";
        final String player1 = "Player-2";
        final String player2 = "Player-3";
        final String char1 = "char";
        final String char2 = "char1";
        final String char3 = "char2";
        final PlayerState previousState = PlayerState.NOT_READY;
        final PlayerState updateState = PlayerState.READY;
        CharacterSuggestion character = new CharacterSuggestion(char1, "John");
        CharacterSuggestion character1 = new CharacterSuggestion(char2, "Duke");
        CharacterSuggestion character2 = new CharacterSuggestion(char3, "Nick");
        gameService.enrollToGame(gameId, player);
        gameService.enrollToGame(gameId, player1);
        gameService.enrollToGame(gameId, player2);
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
        System.out.println(gameService.findByIdAndPlayer(gameId, player).get()
                .getPlayers().stream().map(PlayerWithState::getPlayer).collect(Collectors.toList()));
        System.out.println();
        Map<String, PlayerWithState> playerWithStateMap = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().stream()
                .collect(Collectors.toMap(a-> a.getPlayer().getId(), Function.identity()));
        assertEquals(updateState, playerState);
        assertNotEquals(char1,playerWithStateMap.get(player).getPlayer().getCharacter());
        assertNotEquals(char2,playerWithStateMap.get(player1).getPlayer().getCharacter());
        assertNotEquals(char3,playerWithStateMap.get(player2).getPlayer().getCharacter());

        System.out.println(this.gameRepository.findById(gameId).get().getStatus());

    }
    @Test
    @SneakyThrows
    void testQuestion() {
        final String player = "Player-1";
        final String player1 = "Player-2";
        final String player2 = "host";
        final String player3 = "host1";
        final String char1 = "char";
        final String char2 = "char1";
        final String char3 = "char2";
        final String char4 = "char3";
        final PlayerState previousState = PlayerState.NOT_READY;
        final PlayerState updateState = PlayerState.READY;
        CharacterSuggestion character = new CharacterSuggestion(char1, player);
        CharacterSuggestion character1 = new CharacterSuggestion(char2, player1);
        CharacterSuggestion character2 = new CharacterSuggestion(char3, player2);
        CharacterSuggestion character3 = new CharacterSuggestion(char4, player3);
        final String question = "Am i a human";
        Runnable task1 = () -> {
            gameService.enrollToGame(gameId, player);
            gameService.enrollToGame(gameId, player1);
            gameService.enrollToGame(gameId, player3);
            PlayerState playerState = this.gameRepository.findById(gameId)
                    .filter(game -> game.findPlayer(player).isPresent())
                    .map(GameDetails::of).get().getPlayers().get(0).getState();
            assertEquals(playerState, previousState);
            gameService.suggestCharacter(gameId, player, character);
            gameService.suggestCharacter(gameId, player1, character1);
            gameService.suggestCharacter(gameId, player2, character2);
            gameService.suggestCharacter(gameId, player3, character3);
        };

        Thread thread1 = new Thread(task1);
         thread1.start();

        Runnable task2 = () -> {
            try {
                Thread.sleep(1000);
                gameService.askQuestion(gameId, player3, question);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };


        Thread thread = new Thread(task2);
        thread.start();
        Thread.sleep(2000);
        Map<String, PlayerWithState> playerWithStateMap = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().stream()
                .collect(Collectors.toMap(a -> a.getPlayer().getId(), Function.identity()));
        assertEquals(player3, this.gameService.findByIdAndPlayer(gameId,player3).get().getCurrentTurn());
        assertNotNull(playerWithStateMap.get(player3).getPlayer().getFirstQuestion());
        assertEquals(question, playerWithStateMap.get(player3).getPlayer().getFirstQuestion().get());

    }
    @Test
    @SneakyThrows
    void testQuestionAndAnswer() {
        final String player = "Player-1";
        final String player1 = "Player-2";
        final String player2 = "host";
        final String player3 = "host1";
        final String char1 = "char";
        final String char2 = "char1";
        final String char3 = "char2";
        final String char4 = "char3";
        final PlayerState previousState = PlayerState.NOT_READY;
        final PlayerState updateState = PlayerState.READY;
        CharacterSuggestion character = new CharacterSuggestion(char1, player);
        CharacterSuggestion character1 = new CharacterSuggestion(char2, player1);
        CharacterSuggestion character2 = new CharacterSuggestion(char3, player2);
        CharacterSuggestion character3 = new CharacterSuggestion(char4, player3);
        final String question = "Am i a human";
        Runnable task1 = () -> {
            gameService.enrollToGame(gameId, player);
            gameService.enrollToGame(gameId, player1);
            gameService.enrollToGame(gameId, player3);
            PlayerState playerState = this.gameRepository.findById(gameId)
                    .filter(game -> game.findPlayer(player).isPresent())
                    .map(GameDetails::of).get().getPlayers().get(0).getState();
            assertEquals(playerState, previousState);
            gameService.suggestCharacter(gameId, player, character);
            gameService.suggestCharacter(gameId, player1, character1);
            gameService.suggestCharacter(gameId, player2, character2);
            gameService.suggestCharacter(gameId, player3, character3);
        };

        Thread thread1 = new Thread(task1);
        thread1.start();

        Runnable task2 = () -> {
            try {
                Thread.sleep(100);
                gameService.askQuestion(gameId, player3, question);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };


        Thread thread = new Thread(task2);
        thread.start();
        Thread.sleep(200);

        final String answer = "YES";
        final String answer1 = "YES";
        final String answer2 = "NO";
         task2 = () -> {
            try {
                Thread.sleep(100);
                gameService.answerQuestion(gameId, player, answer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Runnable task3 = () -> {
            try {
                Thread.sleep(100);
                gameService.answerQuestion(gameId, player1, answer1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Runnable task4 = () -> {
            try {
                Thread.sleep(100);
                gameService.answerQuestion(gameId, player2, answer2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread4 = new Thread(task4);
        thread4.start();
        Thread thread3 = new Thread(task3);
         thread = new Thread(task2);
        thread.start();
        thread3.start();
        Thread.sleep(200);
        Map<String, PlayerWithState> playerWithStateMap = this.gameRepository.findById(gameId)
                .filter(game -> game.findPlayer(player).isPresent())
                .map(GameDetails::of).get().getPlayers().stream()
                .collect(Collectors.toMap(a -> a.getPlayer().getId(), Function.identity()));
        assertEquals(player3, this.gameService.findByIdAndPlayer(gameId,player3).get().getCurrentTurn());
        assertNotNull(playerWithStateMap.get(player3).getPlayer().getFirstQuestion());
        assertEquals(question, playerWithStateMap.get(player3).getPlayer().getFirstQuestion().get());
        assertEquals(answer, playerWithStateMap.get(player).getPlayer().getCurrentAnswer().get());
        assertEquals(answer1, playerWithStateMap.get(player1).getPlayer().getCurrentAnswer().get());
        assertEquals(answer2, playerWithStateMap.get(player2).getPlayer().getCurrentAnswer().get());
        Thread.sleep(200);
        System.out.println(this.gameService.findByIdAndPlayer(gameId,player3).get().getCurrentTurn());
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
