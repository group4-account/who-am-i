package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.TurnDetails;
import com.eleks.academy.whoami.repository.GameRepository;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

	private final GameRepository gameRepository;

	@Override
	public List<GameLight> findAvailableGames(String player) {
		return this.gameRepository.findAllAvailable(player)
				.map(GameLight::of)
				.toList();
	}

	@Override
	public Optional<GameDetails> findAvailableQuickGame(String player) {
		Map<String, SynchronousGame> games = gameRepository.findAvailableQuickGames();

		if (games.isEmpty()) {
			GameDetails game = createQuickGame();
			enrollToGame(game.getId(), player);
			return gameRepository.findById(game.getId()).map(GameDetails::of);
		}

		String firstGame = games.keySet().stream().findFirst().get();
		Optional<SynchronousPlayer> enrolledPlayer = enrollToGame(games.get(firstGame).getId(), player);
		String gameFromRepository = games.get(firstGame).getId();
		return gameRepository.findById(gameFromRepository).map(GameDetails::of);
	}

	private GameDetails createQuickGame() {
		return GameDetails.of(gameRepository.save(new PersistentGame(4)));
	}


	@Override
	public GameDetails createGame(String player, NewGameRequest gameRequest) {
		final var game = this.gameRepository.save(new PersistentGame(player, gameRequest.getMaxPlayers()));

		return GameDetails.of(game);
	}

	@Override
	public Optional<SynchronousPlayer> enrollToGame(String id, String player) {
		this.gameRepository.findById(id)
				.filter(SynchronousGame::isAvailable)
				.ifPresentOrElse(
						game -> game.makeTurn(new Answer(player)),
						() -> {
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot enroll to a game");
						}
				);
		return this.gameRepository.findById(id)
				.map(game -> game.findPlayer(player).orElseThrow());
	}

	@Override
	public Optional<GameDetails> findByIdAndPlayer(String id, String player) {
		return this.gameRepository.findById(id)
				.filter(game -> game.findPlayer(player).isPresent())
				.map(GameDetails::of);
	}

	@Override
	public void suggestCharacter(String id, String player, CharacterSuggestion suggestion) {
		this.gameRepository.findById(id)
				.flatMap(game -> game.findPlayer(player))
				.ifPresent(p -> p.setCharacter(suggestion.getCharacter()));
	}

	@Override
	public Optional<GameDetails> startGame(String id, String player) {
		return this.gameRepository.findById(id)
				.map(SynchronousGame::start)
				.map(GameDetails::of);
	}

	@Override
	public void askQuestion(String gameId, String player, String message) {
		this.gameRepository.findById(gameId)
				.ifPresent(game -> game.askQuestion(player, message));
	}

	@Override
	public Optional<TurnDetails> findTurnInfo(String id, String player) {
		var currentGame = gameRepository.findById(id);
		var answers = currentGame.map(x -> x.getCurrentTurnInfo())
				.orElse(Optional.empty());

		var currentPlayer = answers
				.map(answer -> answer.findPlayer(player))
				.orElse(Optional.empty());

		return answers
					.map(gamestate -> new TurnDetails(
							currentPlayer.get(),
							answers.get().getPlayersWithState()));

	}

	@Override
	public void submitGuess(String id, String player, String guess) {

	}

	@Override
	public void answerQuestion(String id, String player, String answer) {

	}

	@Override
	public int getPlayersCount(String id, String player) {
		return this.gameRepository.findById(id)
				.map(item -> item.getPlayersInGame().size())
				.orElse(0);

	}
	@Override
	public int getReadyPlayersCount(String id, String player) {
		return this.gameRepository.findById(id)
				.map(currentGame -> (int) currentGame.getPlayersInGame()
						.stream()
						.filter(p -> p.getState().equals(PlayerState.READY))
						.count())
				.orElse(0);

	}

}
