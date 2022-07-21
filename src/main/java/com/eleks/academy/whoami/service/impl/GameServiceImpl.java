package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.TurnDetails;
import com.eleks.academy.whoami.repository.AddAnswerRequest;
import com.eleks.academy.whoami.repository.AddQuestionRequest;
import com.eleks.academy.whoami.repository.GameRepository;
import com.eleks.academy.whoami.repository.QNAHistoryRepository;
import com.eleks.academy.whoami.repository.impl.QNAHistoryRepositoryImpl;
import com.eleks.academy.whoami.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
	private final GameRepository gameRepository;
	private final QNAHistoryRepository qnaHistoryRepository;

	@Override
	public List<GameLight> findAvailableGames(String player) {
		return this.gameRepository.findAllAvailable(player)
				.map(GameLight::of)
				.toList();
	}

	@Override
	public Optional<GameDetails> findAvailableQuickGame(String player) {
		Map<String, SynchronousGame> games = this.gameRepository.findAvailableQuickGames();

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

	@Override
	public int getAllPlayersCount() {
		return this.gameRepository.getAllPlayersCount();
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

	private GameDetails createQuickGame() {
		int playersSize = 4;
		return GameDetails.of(gameRepository.save(new PersistentGame(playersSize)));
	}

	@Override
	public GameDetails createGame(String player, NewGameRequest gameRequest) {
		final var game = this.gameRepository.save(new PersistentGame(player, gameRequest.getMaxPlayers()));

		return GameDetails.of(game);
	}

	@Override
	public List<QNAHistoryRepositoryImpl.Question> getQnaHistory(String gameId){
		var history = new ArrayList<>(qnaHistoryRepository.GetGameHistory(gameId));
		history.stream().filter(x -> x.isActiveQuestion).forEach(x -> x.Answers = new ArrayList<>());
		return history;
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
				.filter(game -> ofNullable(game.findPlayer(player))
						.map(Optional::isPresent)
						.orElse(false))
				.map(GameDetails::of);
	}

	@Override
	public void suggestCharacter(String id, String player, CharacterSuggestion suggestion) {
		this.gameRepository.findById(id)
				.ifPresentOrElse(
						game -> game.makeTurn(new Answer(player, suggestion.getCharacter(), suggestion.getName())),
						() -> {
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot enroll to a game");
						}
				);

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

		this.gameRepository.findById(gameId).stream()
				.findFirst()
				.map(SynchronousGame::getPlayersInGame)
				.ifPresent(pwsl -> this.qnaHistoryRepository.AddQuestionRequest(new AddQuestionRequest(false, gameId, player, message), pwsl));
	}

	@Override
	public Optional<TurnDetails> findTurnInfo(String id, String player) {

		final var currentGame = gameRepository.findById(id);
		var answers = currentGame
				.flatMap(SynchronousGame::getCurrentTurnInfo);

		var currentPlayer = answers
				.flatMap(answer -> answer.findPlayer(player))
				.orElseThrow(() -> new NoSuchElementException("Player has not answered"));

		return answers
				.map(gamestate -> new TurnDetails(
						currentPlayer,
						answers.map(GameState::getPlayersWithState)
								.orElse(new ArrayList<>())));
	}

	@Override
	public void submitGuess(String id, String player, QuestionAnswer guess) {

	}

	@Override
	public void answerQuestion(String gameId, String player, String answer) {
		this.gameRepository.findById(gameId)
				.ifPresent(game -> game.answerQuestion(player, answer));
		this.qnaHistoryRepository.AddAnswerRequest(new AddAnswerRequest(false, gameId, player, QuestionAnswer.valueOf(answer)));
	}

	@Override
	public void leaveGame(String gameId, String playerId) {
		SynchronousGame game = this.gameRepository.findById(gameId)
				.orElseThrow(
						() -> new GameException(String.format("ROOM_NOT_FOUND_BY_ID", gameId)));
		game.removeFromGame(gameId, playerId);
		if (ofNullable(game.getPlayersInGame())
				.map(List::size)
				.map(size -> size.equals(0))
				.orElse(false)) {
			this.gameRepository.remove(game);
		}

	}
}
