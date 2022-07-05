package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.core.impl.PersistentPlayer;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static com.eleks.academy.whoami.model.request.QuestionAnswer.*;
import static com.eleks.academy.whoami.model.response.PlayerState.ANSWERING;
import static com.eleks.academy.whoami.model.response.PlayerState.ASKING;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.SECONDS;

// TODO: Implement makeTurn(...) and next() methods, pass a turn to next player
public final class ProcessingQuestion extends AbstractGameState {

	private final Map<String, PlayerWithState> players;

	ExecutorService executorService = Executors.newSingleThreadExecutor();

	public ProcessingQuestion(String currentPlayer1, Map<String, PlayerWithState> players) {
		super(players.size(), players.size());
		final String currentPlayer = currentPlayer1;
		this.players = players;

		resetToDefault();
		players.get(currentPlayer).setState(ASKING);
		players.values().stream()
				.filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), currentPlayer))
				.forEach(player -> player.setState(ANSWERING));
		players.values().stream()
				.filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), currentPlayer))
				.forEach(player -> player.getPlayer().setReadyForAnswerFuture(completedFuture(false)));
		supplyAsync(() -> this.makeTurn(new Answer(null)), executorService);
	}

	@Override
	public GameState next() {
		throw new GameException("Not implemented");
	}

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		return ofNullable(this.players.get(player))
				.map(PlayerWithState::getPlayer);
	}

	@Override
	public String getCurrentTurn() {
		return this.players.values().stream()
				.filter(player -> Objects.equals(player.getState(), ASKING))
				.findFirst()
				.map(a -> a.getPlayer().getId())
				.orElseThrow();
	}

	@Override
	public List<PlayerWithState> getPlayersWithState() {
		return players.values().stream().toList();
	}


	@Override
	public GameState makeTurn(Answer answerQuestion) {
		PlayerWithState currentPlayer = players.get(getCurrentTurn());
		try {
			try {
				players.get(currentPlayer.getPlayer().getId())
						.getPlayer().getFirstQuestion().get(20, SECONDS);
			} catch (TimeoutException e) {
				List<String> collect = new ArrayList<>(this.players.keySet());
				return new ProcessingQuestion(collect.get(findCurrentPlayerIndex(collect)), players);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		this.players.values()
				.stream()
				.filter(playerWithState -> Objects.equals(playerWithState.getState(), ANSWERING))
				.parallel()
				.forEach(player1 -> {
			try {
				try {
					player1.setAnswer(QuestionAnswer.valueOf(player1.getPlayer().answerQuestion().get(20, SECONDS)));
				} catch (TimeoutException e) {
					player1.setAnswer(NOT_SURE);
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		});
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long noAnswers = this.players.values().stream()
				.filter(player -> Objects.equals(player.getState(), ANSWERING))
				.filter(playerWithState -> Objects.equals(playerWithState.getAnswer(), NO))
				.count();
		long yesAnswers = this.players.values().stream()
				.filter(player -> Objects.equals(player.getState(), ANSWERING))
				.filter(playerWithState -> Objects.equals(playerWithState.getAnswer(), YES))
				.count();
		if (yesAnswers < noAnswers) {
			List<String> collect = new ArrayList<>(this.players.keySet());
			return new ProcessingQuestion(collect.get(findCurrentPlayerIndex(collect)), players);
		} else {
			return new ProcessingQuestion(currentPlayer.getPlayer().getId(), players);
		}
	}


	@Override
	public GameState makeLeave(Answer answer) {
		List<String> playersList = new ArrayList<>(this.players.keySet());
		if (isAskingPlayer(answer)) {
			return new ProcessingQuestion(playersList.get(findCurrentPlayerIndex(playersList)), players);
		} else {
			//TODO: add remove player from list
			return this;
		}
	}

	private boolean isAskingPlayer(Answer answer) {
		return Objects.equals(answer.getPlayer(),
				this.players.values()
						.stream()
						.filter(player -> Objects.equals(player.getState(), ASKING))
						.findFirst()
						.map(playerWithState -> playerWithState.getPlayer().getId())
						.orElse("NOT_ASKING_PLAYER"));
	}

	private int findCurrentPlayerIndex(List<String> playersList) {
		PlayerWithState currentPlayer = players.get(getCurrentTurn());
		int currentPlayerIndex;
		currentPlayerIndex = playersList.indexOf(currentPlayer.getPlayer().getName());
		return currentPlayerIndex + 1 >= this.players.size() ? 0 : currentPlayerIndex + 1;
	}
	private void resetToDefault() {
		players.values().forEach(playerWithState -> {
			playerWithState.setAnswer(null);
			playerWithState.getPlayer().setQuestion(null);
			ofNullable(playerWithState.getPlayer())
					.map(PersistentPlayer::inCompleteFuture);
		});
	}

}
