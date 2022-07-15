package com.eleks.academy.whoami.core.state;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.Answer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.eleks.academy.whoami.model.request.QuestionAnswer.NO;
import static com.eleks.academy.whoami.model.request.QuestionAnswer.NOT_SURE;
import static com.eleks.academy.whoami.model.response.PlayerState.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.partitioningBy;

public final class ProcessingQuestion extends AbstractGameState {

	private Map<String, PlayerWithState> players;
	private volatile long timer;
	private final int maxTimeForQuestion = 60;
	private final int maxTimeForAnswer = 20;

	public ProcessingQuestion(String currentPlayer1, Map<String, PlayerWithState> players) {
		super(players.size(), players.size());
		this.players = players;
		this.players.values()
				.stream()
				.filter(playerWithState -> playerWithState.getPlayer().getBeingInActiveCount() == 3)
				.forEach(player -> this.leaveGame(player, currentPlayer1));

		final String currentPlayer = currentPlayer1;
		this.players.get(currentPlayer).setState(ASKING);
		this.players.values().stream()
				.filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(), currentPlayer))
				.forEach(player -> player.setState(READY));

		runAsync(() -> this.makeTurn(new Answer(null)));
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
				.map(playerWithState -> playerWithState.getPlayer().getId())
				.orElse("No one asking at this time");
	}

	@Override
	public List<PlayerWithState> getPlayersWithState() {
		return ofNullable(this.players)
				.map(Map::values)
				.map(Collection::stream)
				.map(Stream::toList)
				.orElse(new ArrayList<>());
	}

	@Override
	public Optional<PlayerWithState> findPlayerWithState(String player) {
		return ofNullable(this.players.get(player));
	}

	@Override
	public GameState makeTurn(Answer answerQuestion) {
		resetToDefault();
		PlayerWithState currentPlayer = players.get(getCurrentTurn());
		try {
			try {
				currentPlayer.getFutureQuestion().get(maxTimeForQuestion, SECONDS);
			} catch (TimeoutException e) {
				Map<String, PlayerWithState> newPlayersMap = this.players;
				newPlayersMap.remove(currentPlayer.getPlayer().getId());
				List<String> playersList = new ArrayList<>(newPlayersMap.keySet());
				return new ProcessingQuestion(playersList
						.get(findCurrentPlayerIndex(playersList, currentPlayer)), newPlayersMap);
			} finally {
				if (currentPlayer.getQuestion() != null)
					this.players.values().stream()
							.filter(playerWithState -> !Objects.equals(playerWithState.getPlayer().getId(),
									currentPlayer.getPlayer().getId()))
							.forEach(player -> player.setState(ANSWERING));
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		this.players.values()
				.parallelStream()
				.filter(playerWithState -> Objects.equals(playerWithState.getState(), ANSWERING))
				.forEach(player1 -> {
					try {
						try {
							player1.answerQuestion().get(maxTimeForAnswer, SECONDS);
							player1.getPlayer().zeroTimePlayersBeingInactive();
						} catch (TimeoutException e) {
							player1.getPlayer().incrementBeingInactiveCount();
						} finally {
							this.players.values()
									.stream().filter(playerWithState -> !playerWithState.getCurrentAnswer().isDone()
											&& playerWithState.getState().equals(ANSWERING))
									.forEach(playerWithState -> playerWithState.setAnswer("NOT_SURE"));
						}
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				});
		Map<Boolean, List<PlayerWithState>> booleanPlayersAnswerMap = this.players.values().stream()
				.filter(player -> Objects.equals(player.getState(), ANSWERING))
				.collect(partitioningBy(playerWithState -> Objects.equals(playerWithState.getAnswer(), NO)));
		if (booleanPlayersAnswerMap.get(FALSE).size() < booleanPlayersAnswerMap.get(TRUE).size()) {
			List<String> collect = new ArrayList<>(this.players.keySet());
			return new ProcessingQuestion(collect.get(findCurrentPlayerIndex(collect, currentPlayer)), players);
		} else {
			return new ProcessingQuestion(currentPlayer.getPlayer().getId(), players);
		}
	}

	@Override
	public GameState leaveGame(String answer) {
		List<String> playersList = new ArrayList<>(this.players.keySet());
		var nextCurrentPlayerIndex = findCurrentPlayerIndex(playersList,
				this.players.get(getCurrentTurn())) + 1 % playersList.size();
		var nextCurrentPlayer = playersList.get(nextCurrentPlayerIndex);

		if (isAskingPlayer(answer)) {
			this.players.remove(answer);
			return new ProcessingQuestion(nextCurrentPlayer, players);
		} else {
			this.players.remove(answer);
			return this;
		}
	}

	@Override
	public long getTimer() {
		return timer;
	}

	private void leaveGame(PlayerWithState playerWithState, String currentPlayer) {
		Map<String, PlayerWithState> newPlayersMap = this.players;
		if (isAskingPlayer(playerWithState.getPlayer().getId())) {
			newPlayersMap.remove(currentPlayer);
		} else {
			newPlayersMap.remove(playerWithState.getPlayer().getId());
		}
		this.players = newPlayersMap;
	}

	private boolean isAskingPlayer(String answer) {
		return Objects.equals(answer,
				this.players.values()
						.stream()
						.filter(player -> Objects.equals(player.getState(), ASKING))
						.findFirst()
						.map(playerWithState -> playerWithState.getPlayer().getId())
						.orElse("NOT_ASKING_PLAYER"));
	}

	private int findCurrentPlayerIndex(List<String> playersList, PlayerWithState currentPlayer) {
		return Stream.of(playersList.indexOf(currentPlayer.getPlayer().getId()))
				.map(playerIndex -> playerIndex + 1 >= this.players.size() ? 0 : playerIndex + 1)
				.findFirst()
				.orElseThrow();
	}

	private void resetToDefault() {
		this.players.values().forEach(PlayerWithState::inCompleteFuture);
	}

	//
	private void startTimer() {
		int limit = maxTimeForQuestion;
		long start = currentTimeMillis();
		boolean flag = true;
		while (!this.players.isEmpty()) {
			timer = 1;
			boolean isQuestionPresent = false;
			while (timer > 0) {
				long now = currentTimeMillis();
				timer = limit - MILLISECONDS.toSeconds(now - start);
				if (this.players.values().stream()
						.anyMatch(player -> player.getState().equals(ANSWERING)) && flag) {
					isQuestionPresent = true;
					break;
				}
			}
			if (isQuestionPresent) {
				limit = maxTimeForAnswer;
				flag = false;
			} else {
				limit = maxTimeForQuestion;
			}
			start = currentTimeMillis();
		}
	}

}
